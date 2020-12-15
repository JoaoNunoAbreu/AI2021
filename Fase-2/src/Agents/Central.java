package Agents;
import Util.InformPosition;
import Util.Mapa;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Central extends Agent {

	private Mapa mapa;

	protected void setup() {
		super.setup();
		System.out.println("My name is "+ getLocalName());

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("central");
		sd.setName(getLocalName());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}

		this.mapa = (Mapa) getArguments()[0];
		this.addBehaviour(new Receiver());
	}

	protected void takeDown() {
		super.takeDown();
	}

	private class Receiver extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {
					try {
						System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName() + " registered!");

						InformPosition content = (InformPosition) msg.getContentObject();
						mapa.addNewEstacao(content.getPosition());

					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
			}
			else block();
		}
	}
}