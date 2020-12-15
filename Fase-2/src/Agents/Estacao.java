package Agents;
import Util.InformPosition;
import Util.Posicao;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Estacao extends Agent {

	private int num_bicicletas;
	private List<Posicao> estacoes;
	private InformPosition current_location;

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

		this.num_bicicletas = (int) getArguments()[0];
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
						current_location = new InformPosition(myAgent.getAID(), p);

						ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
						msg.setContentObject(current_location);

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
					System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName()  + " fez um aluguer!");

					if(num_bicicletas > 0){
						System.out.println(myAgent.getAID().getLocalName() + " - Aluguer aceite!");
						num_bicicletas--;
						ACLMessage mensagem = msg.createReply();
						mensagem.setPerformative(ACLMessage.CONFIRM);
						myAgent.send(mensagem);
					}
					else{
						System.out.println(myAgent.getAID().getLocalName() + " - Aluguer não aceite!");
						ACLMessage mensagem = msg.createReply();
						mensagem.setPerformative(ACLMessage.REFUSE);
						myAgent.send(mensagem);
					}
				}
				else{
					System.out.println("Mensagem recebida no getLocalName() tem erro no performative (" + msg.getPerformative() + ")");
				}
			}
			else {
				block();
			}
		}
	}
}