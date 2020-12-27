package Agents;
import Util.*;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.List;
import java.util.Random;

public class Estacao extends Agent {

	private List<Posicao> estacoes;
	private InfoEstacao infoestacao;
	private IO io;

	protected void setup() {
		super.setup();
		this.io = new IO();

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("estacao");
		sd.setName(getLocalName());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.estacoes = (List<Posicao>) getArguments()[1];
		this.addBehaviour(new Register());
		this.addBehaviour(new Receiver());
	}

	protected void takeDown() {
		super.takeDown();
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	private class Register extends OneShotBehaviour {

		private Posicao getRandPosition() {
			Random rand = new Random();
			float x = rand.nextInt((int) getArguments()[2]);
			float y = rand.nextInt((int) getArguments()[2]);
			return new Posicao(x,y);
		}

		private boolean demasiadoJuntos(Posicao posicao) {
			int proximidade = 5; // Valor arbitrário de proximidade
			for(Posicao p : estacoes){
				if(posicao.distanceBetween(p) < proximidade)
					return true;
			}
			return false;
		}

		public Posicao geraPosEstacao(){
			int tentativas = 10;
			Posicao p = null;
			boolean valid = false;
			for(int j = 0; j < tentativas && !valid; j++){
				p = getRandPosition();
				if(!demasiadoJuntos(p))
					valid = true;
			}
			if(!valid)
				p = null;
			return p;
		}

		public void action() {

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("central");
			template.addServices(sd);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				// If Central is available!
				if (result.length > 0) {
					Posicao p = geraPosEstacao();
					if(p == null)
						io.writeToLogs("Erro ao criar a " + myAgent.getLocalName());
					else{
						int num_bicicletas = (int) getArguments()[0];
						int max_bicicletas = (int) getArguments()[3];

						infoestacao = new InfoEstacao(myAgent.getAID(), p, num_bicicletas,max_bicicletas, (int) getArguments()[2] / 2);

						ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
						msg.setContentObject(infoestacao);

						for (int i = 0; i < result.length; ++i)
							msg.addReceiver(result[i].getName());

						myAgent.send(msg);
					}
				}
				// No Central is available - kill the agent!
				else
					io.writeToLogs(myAgent.getAID().getLocalName() + " - No Central available. Agent offline");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	private class Receiver extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				try {
					if (msg.getPerformative() == ACLMessage.REQUEST) {
						InfoUtilizador infoutilizador = (InfoUtilizador) msg.getContentObject();

						// Se for pedido de aluguer
						if(infoutilizador.getInit().equals(infoestacao.getPosition()))
							addBehaviour(new HandlePedidoAluguer(msg));

						// Se for pedido de devolução
						else if(infoutilizador.getDest().equals(infoestacao.getPosition()))
							addBehaviour(new HandlePedidoDevolucao(msg));

						else
							io.writeToLogs(myAgent.getAID().getLocalName()+" recebeu request inválido.");
					}
					else if (msg.getPerformative() == ACLMessage.INFORM){
						// Envia incentivo ao utilizador que está na sua APE
						addBehaviour(new EnviaIncentivo(msg));
					}
					else
						io.writeToLogs("Mensagem recebida no " + myAgent.getLocalName() + "tem erro no performative (" + msg.getPerformative() + ")");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else block();
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	private class HandlePedidoAluguer extends OneShotBehaviour{

		private ACLMessage msg;

		public HandlePedidoAluguer(ACLMessage msg) {
			this.msg = msg;
		}

		@Override
		public void action() {
			try {
				io.writeToLogs(myAgent.getAID().getLocalName() + " - " + msg.getSender().getLocalName() + " fez um pedido de aluguer!");

				// Há bicicletas suficientes para alugar
				if (infoestacao.getNum_bicicletas() > 0) {
					io.writeToLogs(myAgent.getAID().getLocalName() + " - Pedido de aluguer aceite a " + msg.getSender().getLocalName() + "!");
					infoestacao.decrement();
					ACLMessage mensagem = msg.createReply();
					mensagem.setPerformative(ACLMessage.CONFIRM);

					myAgent.send(mensagem);
				}
				// Não há bicicletas para alugar
				else {
					io.writeToLogs(myAgent.getAID().getLocalName() + " - Pedido de aluguer rejeitado a " + msg.getSender().getLocalName() + "!");
					ACLMessage mensagem = msg.createReply();
					mensagem.setPerformative(ACLMessage.REFUSE);
					myAgent.send(mensagem);
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	private class HandlePedidoDevolucao extends OneShotBehaviour{

		private ACLMessage msg;

		public HandlePedidoDevolucao(ACLMessage msg) {
			this.msg = msg;
		}

		@Override
		public void action() {
			try{
				io.writeToLogs(myAgent.getAID().getLocalName() + " - " + msg.getSender().getLocalName() + " fez um pedido de devolução!");

				// Há espaço para mais uma bicicleta
				if (infoestacao.getNum_bicicletas() < infoestacao.getNum_bicicletas_max()) {
					io.writeToLogs(myAgent.getAID().getLocalName() + " - Pedido de devolução aceite a "+ msg.getSender().getLocalName()+ "!");
					infoestacao.increment();
					ACLMessage mensagem = msg.createReply();
					mensagem.setPerformative(ACLMessage.CONFIRM);
					myAgent.send(mensagem);
				}
				// Não há espaço para mais nenhuma bicicleta
				else {
					io.writeToLogs(myAgent.getAID().getLocalName() + " - Pedido de devolução rejeitado a "+ msg.getSender().getLocalName()+ "!");
					ACLMessage mensagem = msg.createReply();
					mensagem.setPerformative(ACLMessage.REFUSE);
					myAgent.send(mensagem);
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	private class EnviaIncentivo extends OneShotBehaviour{

		private ACLMessage msg;

		public EnviaIncentivo(ACLMessage msg) {
			this.msg = msg;
		}

		@Override
		public void action() {
			try {
				InfoUtilizador infoutilizador = (InfoUtilizador) msg.getContentObject();
				ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
				Incentivo i = new Incentivo(infoestacao.getPosition(), infoestacao.incentivo());
				mensagem.setContentObject(i);
				mensagem.addReceiver(infoutilizador.getAgent());
				myAgent.send(mensagem);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}