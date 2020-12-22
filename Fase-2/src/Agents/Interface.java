package Agents;
import Util.Mapa;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class Interface extends Agent {

    private Mapa mapa;

    protected void setup() {
        super.setup();
        System.out.println("My name is "+ getLocalName());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("interface");
        sd.setName(getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        this.addBehaviour(new ReceiveMap());
    }

    protected void takeDown() {
        super.takeDown();
    }

    // -----------------------------------------------------------------------------------------------------------------

    private class ReceiveMap extends CyclicBehaviour {

        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                try {
                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        mapa = (Mapa) msg.getContentObject();
                        System.out.println(mapa.toString());
                    }
                    else{
                        System.out.println("Agente interface recebeu um pedido inválido!");
                    }
                }
                catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
            else block();
        }
    }
}
