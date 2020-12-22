package Agents;
import Util.Incentivo;
import Util.InfoEstacao;
import Util.InfoUtilizador;
import Util.Posicao;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Estacao extends Agent {

	private List<Posicao> estacoes;
	private InfoEstacao infoestacao;

	protected void setup() {
		super.setup();
		System.out.println("My name is "+ getLocalName());

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("estacao");
		sd.setName(getLocalName());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		this.estacoes = (List<Posicao>) getArguments()[1];
		this.addBehaviour(new Register());
		this.addBehaviour(new Receiver());
	}

	protected void takeDown() {
		super.takeDown();
	}

	// -----------------------------------------------------------------------------------------------------------------

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
					if(p == null){
						System.out.println("Não foi possível criar esta estação...");
					}
					else{
						int num_bicicletas = (int) getArguments()[0];
						int max_bicicletas = (int) getArguments()[3];

						infoestacao = new InfoEstacao(myAgent.getAID(), p, num_bicicletas,max_bicicletas);

						ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
						msg.setContentObject(infoestacao);

						for (int i = 0; i < result.length; ++i) {
							msg.addReceiver(result[i].getName());
						}
						myAgent.send(msg);
					}
				}
				// No Central is available - kill the agent!
				else {
					System.out.println(myAgent.getAID().getLocalName() + ": No Central available. Agent offline");
				}

			} catch (IOException | FIPAException e) {
				e.printStackTrace();
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	private class Receiver extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.REQUEST) {
					try {
						InfoUtilizador infoutilizador = (InfoUtilizador) msg.getContentObject();

						if(infoutilizador.getInit().equals(infoestacao.getPosition())) {
							System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName() + " fez um pedido de aluguer!");

							if (infoestacao.getNum_bicicletas() > 0) {
								System.out.println(myAgent.getAID().getLocalName() + " - Aluguer aceite!");
								infoestacao.decrement();
								ACLMessage mensagem = msg.createReply();
								mensagem.setPerformative(ACLMessage.CONFIRM);

								myAgent.send(mensagem);

							} else {
								System.out.println(myAgent.getAID().getLocalName() + " - Aluguer rejeitado!");
								ACLMessage mensagem = msg.createReply();
								mensagem.setPerformative(ACLMessage.REFUSE);
								myAgent.send(mensagem);
							}
						}
						else if(infoutilizador.getDest().equals(infoestacao.getPosition())) {
							System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName() + " fez um pedido de devolução!");

							if (infoestacao.getNum_bicicletas() < infoestacao.getNum_bicicletas_max()) {
								System.out.println(myAgent.getAID().getLocalName() + " - Devolução aceite!");
								infoestacao.increment();
								ACLMessage mensagem = msg.createReply();
								mensagem.setPerformative(ACLMessage.CONFIRM);
								myAgent.send(mensagem);
							} else {
								System.out.println(myAgent.getAID().getLocalName() + " - Devolução rejeitada!");
								ACLMessage mensagem = msg.createReply();
								mensagem.setPerformative(ACLMessage.REFUSE);
								myAgent.send(mensagem);
							}
						}
						else{
							System.out.println("Estação recebeu request inválido.");
						}
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
				else if (msg.getPerformative() == ACLMessage.INFORM){
					// Envia incentivo ao utilizador que está na sua APE
					try {
						InfoUtilizador infoutilizador = (InfoUtilizador) msg.getContentObject();
						ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
						Incentivo i = new Incentivo(infoestacao.getPosition(), infoestacao.incentivo());
						mensagem.setContentObject(i);
						mensagem.addReceiver(infoutilizador.getAgent());
						myAgent.send(mensagem);
					}
					catch (UnreadableException | IOException e) {
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
}