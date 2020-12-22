package Agents;
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
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilizador extends Agent {

	private List<Posicao> estacoes;
	private InfoUtilizador infoutilizador;
	private int estacao_destino;

	protected void setup() {
		super.setup();
		System.out.println("My name is "+ getLocalName());
		this.estacoes = (List<Posicao>) getArguments()[0];
		this.addBehaviour(new Request());
		this.addBehaviour(new Reply());
		this.addBehaviour(new AtualizaPosicao(this,10));
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
					while(origem == destino){
						destino = rand.nextInt(estacoes.size());
					}

					estacao_destino = destino;

					Posicao init = estacoes.get(origem);
					Posicao dest = estacoes.get(destino);

					infoutilizador = new InfoUtilizador(myAgent.getAID(), init, dest);
					ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);

					for (int i = 0; i < result.length; ++i) {
						Pattern p1 = Pattern.compile("\\d+");
						Matcher m = p1.matcher(String.valueOf(result[i].getName()));
						String s = "";
						if(m.find()) {
							s = m.group();
						}
						if(Integer.parseInt(s) == origem) {
							mensagem.addReceiver(result[i].getName());
						}
					}

					mensagem.setContentObject(infoutilizador);
					myAgent.send(mensagem);
					System.out.println("Acabou um pedido de aluguer...");
				}
				else{
					System.out.println("Não se fez aluguer");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private class Reply extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();

				if(msg.getPerformative() == ACLMessage.CONFIRM) {
					sd.setType("central");
					template.addServices(sd);

					try {
						DFAgentDescription[] result = DFService.search(myAgent, template);

						if (result.length > 0) {
							ACLMessage mensagem = null;

							// Aluguer
							if(infoutilizador.getInit().equals(infoutilizador.getAtual())) {
								mensagem = new ACLMessage(ACLMessage.SUBSCRIBE);
							}
							// Devolução
							else{
								mensagem = new ACLMessage(ACLMessage.INFORM);
							}

							for (int i = 0; i < result.length; ++i) {
								mensagem.addReceiver(result[i].getName());
							}
							mensagem.setContentObject(infoutilizador);
							myAgent.send(mensagem);

							// Apagar agente se for uma devolução
							if(infoutilizador.getDest().equals(infoutilizador.getAtual())) {
								myAgent.doDelete();
								System.out.println(myAgent.getLocalName() + " foi apagado.");
							}
						}
					}
					catch (FIPAException | IOException e) {
						e.printStackTrace();
					}
				}
				else if(msg.getPerformative() == ACLMessage.REFUSE){
					// Aluguer
					if(infoutilizador.getInit().equals(infoutilizador.getAtual())) {
						myAgent.doDelete();
					}
					// Devolução -> Faz novo pedido
					else{
						System.out.println("Novo pedido de devolução do " + myAgent.getLocalName());
						sd.setType("estacao");
						template.addServices(sd);
						try {
							DFAgentDescription[] result = DFService.search(myAgent, template);
							if (result.length > 0) {
								ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);
								for (int i = 0; i < result.length; ++i) {
									Pattern p1 = Pattern.compile("\\d+");
									Matcher m = p1.matcher(String.valueOf(result[i].getName()));
									String s = "";
									if(m.find()) {
										s = m.group();
									}
									if(Integer.parseInt(s) == estacao_destino) {
										mensagem.addReceiver(result[i].getName());
									}
								}
								mensagem.setContentObject(infoutilizador);
								myAgent.send(mensagem);
							}
						}
						catch (FIPAException | IOException e) {
							e.printStackTrace();
						}
					}
				}
				// Aceita ou rejeita incentivo
				else if(msg.getPerformative() == ACLMessage.INFORM){
					try {
						Incentivo i = (Incentivo) msg.getContentObject();
						if(infoutilizador.aceitaIncentivo(i)) {
							System.out.println(myAgent.getLocalName() + " aceitou o incentivo (" + infoutilizador.getIncentivo_max() + ") da posição " + infoutilizador.getDest());
							String sender = msg.getSender().getLocalName();
							estacao_destino = Integer.parseInt(String.valueOf(sender.charAt(sender.length() - 1)));
						}
						else
							System.out.println(myAgent.getLocalName() + " rejeitou o incentivo (" + i.getIncentivo() + ") da posição " + i.getPosition());
					}
					catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
				else{
					System.out.println("Mensagem recebida no " + myAgent.getLocalName() + "tem erro no performative (" + msg.getPerformative() + ")");
				}
			}
			else {
				block();
			}
		}
	}

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
					System.out.println("Chegou ao destino " + myAgent.getLocalName());

					sd.setType("estacao");
					template.addServices(sd);

					DFAgentDescription[] result = DFService.search(myAgent, template);
					if (result.length > 0) {
						ACLMessage mensagem = new ACLMessage(ACLMessage.REQUEST);
						for (int i = 0; i < result.length; ++i) {
							Pattern p1 = Pattern.compile("\\d+");
							Matcher m = p1.matcher(String.valueOf(result[i].getName()));
							String s = "";
							if(m.find()) {
								s = m.group();
							}
							if(Integer.parseInt(s) == estacao_destino) {
								mensagem.addReceiver(result[i].getName());
							}
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
						for (int i = 0; i < result.length; ++i) {
							mensagem.addReceiver(result[i].getName());
						}
						mensagem.setContentObject(infoutilizador);
						myAgent.send(mensagem);
					}
				}
			}
			catch (FIPAException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}
