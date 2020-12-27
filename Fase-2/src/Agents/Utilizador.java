package Agents;
import Util.IO;
import Util.Incentivo;
import Util.InfoUtilizador;
import Util.Posicao;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilizador extends Agent {

	private List<Posicao> estacoes;
	private InfoUtilizador infoutilizador;
	private int estacao_destino;
	private IO io;

	protected void setup() {
		super.setup();
		this.io = new IO();
		this.estacoes = (List<Posicao>) getArguments()[0];
		this.addBehaviour(new Request());
		this.addBehaviour(new Reply());
		this.addBehaviour(new AtualizaPosicao(this,50));
	}

	protected void takeDown() {
		super.takeDown();
	}

	private class Request extends OneShotBehaviour {

		public void action() {

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("estacao");
			template.addServices(sd);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);

				// If Manager is available!
				if (result.length > 0) {
					Random rand = new Random();

					int origem = rand.nextInt(estacoes.size());
					int destino = rand.nextInt(estacoes.size());
					while(origem == destino)
						destino = rand.nextInt(estacoes.size());

					estacao_destino = destino;

					Posicao init = estacoes.get(origem);
					Posicao dest = estacoes.get(destino);

					infoutilizador = new InfoUtilizador(myAgent.getAID(), init, dest);
					ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);

					// Faz pedido de aluguer e manda a mensagem para apenas a estação inicial.
					for (int i = 0; i < result.length; ++i) {
						Pattern p1 = Pattern.compile("\\d+");
						Matcher m = p1.matcher(String.valueOf(result[i].getName()));
						String s = "";
						if(m.find())
							s = m.group();
						if(Integer.parseInt(s) == origem)
							mensagem.addReceiver(result[i].getName());
					}
					mensagem.setContentObject(infoutilizador);
					myAgent.send(mensagem);
				}
				else io.writeToLogs("Estação " + estacao_destino + " está offline");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	private class Reply extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				try {
					switch (msg.getPerformative()) {
						case ACLMessage.CONFIRM:
							addBehaviour(new HandleConfirms());
							break;
						case ACLMessage.REFUSE:
							addBehaviour(new HandleRefuses());
							break;
						case ACLMessage.INFORM:
							addBehaviour(new HandleIncentivos(msg));
							break;
						default:
							io.writeToLogs("Mensagem recebida no " + myAgent.getLocalName() + "tem erro no performative (" + msg.getPerformative() + ")");
							break;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else block();
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	private class HandleConfirms extends OneShotBehaviour{

		@Override
		public void action() {
			try {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("central");
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(myAgent, template);

				if (result.length > 0) {
					ACLMessage mensagem;

					// Aluguer
					if (infoutilizador.getInit().equals(infoutilizador.getAtual()))
						mensagem = new ACLMessage(ACLMessage.SUBSCRIBE);
					// Devolução
					else
						mensagem = new ACLMessage(ACLMessage.INFORM);

					for (int i = 0; i < result.length; ++i)
						mensagem.addReceiver(result[i].getName());

					mensagem.setContentObject(infoutilizador);
					myAgent.send(mensagem);

					// Apagar agente se for uma devolução
					if (infoutilizador.getDest().equals(infoutilizador.getAtual())) {
						myAgent.doDelete();
						io.writeToLogs(myAgent.getLocalName() + " saiu do sistema.");
					}
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	private class HandleRefuses extends OneShotBehaviour{

		@Override
		public void action() {
			try {
				// Aluguer
				if (infoutilizador.getInit().equals(infoutilizador.getAtual()))
					myAgent.doDelete();
				// Devolução -> Faz novo pedido
				else {
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("estacao");
					template.addServices(sd);
					DFAgentDescription[] result = DFService.search(myAgent, template);
					if (result.length > 0) {
						ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);
						for (int i = 0; i < result.length; ++i) {
							Pattern p1 = Pattern.compile("\\d+");
							Matcher m = p1.matcher(String.valueOf(result[i].getName()));
							String s = "";
							if (m.find())
								s = m.group();

							if (Integer.parseInt(s) == estacao_destino) {
								mensagem.addReceiver(result[i].getName());
								io.writeToLogs(result[i].getName().getLocalName() + " - " + myAgent.getLocalName() + " reenviou pedido de devolução\n");
							}
						}
						mensagem.setContentObject(infoutilizador);
						myAgent.send(mensagem);
					}
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	public class HandleIncentivos extends OneShotBehaviour{

		private ACLMessage msg;

		public HandleIncentivos(ACLMessage m) {
			this.msg = m;
		}

		@Override
		public void action() {
			try {
				Incentivo i = (Incentivo) msg.getContentObject();
				if (infoutilizador.aceitaIncentivo(i)) {
					io.writeToLogs(myAgent.getLocalName() + " aceitou o incentivo (" + infoutilizador.getIncentivo_max() + ") da posição " + infoutilizador.getDest() + ".");
					String sender = msg.getSender().getLocalName();
					estacao_destino = Integer.parseInt(String.valueOf(sender.charAt(sender.length() - 1)));
				} else
					io.writeToLogs(myAgent.getLocalName() + " rejeitou o incentivo (" + i.getIncentivo() + ") da posição " + i.getPosition() + ".");
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	private class AtualizaPosicao extends TickerBehaviour{

		public AtualizaPosicao(Agent a, long period) {
			super(a, period);
		}

		protected void onTick() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			try {
				// Atualiza nova posição
				infoutilizador.atualiza();

				// Faz pedido de devolução à estação
				if (infoutilizador.getAtual().equals(infoutilizador.getDest())) {

					// Chegou ao destino
					io.writeToLogs(myAgent.getLocalName()+" chegou ao destino.");

					sd.setType("estacao");
					template.addServices(sd);

					DFAgentDescription[] result = DFService.search(myAgent, template);
					if (result.length > 0) {
						ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);
						for (int i = 0; i < result.length; ++i) {
							Pattern p1 = Pattern.compile("\\d+");
							Matcher m = p1.matcher(String.valueOf(result[i].getName()));
							String s = "";
							if(m.find())
								s = m.group();
							if(Integer.parseInt(s) == estacao_destino)
								mensagem.addReceiver(result[i].getName());
						}
						mensagem.setContentObject(infoutilizador);
						myAgent.send(mensagem);
					}
					this.stop();
				}
				else {
					// Vai informando à central a sua posição atual
					sd.setType("central");
					template.addServices(sd);

					DFAgentDescription[] result = DFService.search(myAgent, template);
					if (result.length > 0) {
						ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
						for (int i = 0; i < result.length; ++i)
							mensagem.addReceiver(result[i].getName());
						mensagem.setContentObject(infoutilizador);
						myAgent.send(mensagem);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
