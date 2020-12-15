package Agents;
import Util.MakeRequest;
import Util.Posicao;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilizador extends Agent {

	private Posicao init;
	private Posicao dest;
	private List<Posicao> estacoes;

	protected void setup() {
		super.setup();
		System.out.println("My name is "+ getLocalName());
		this.estacoes = (List<Posicao>) getArguments()[0];
		//this.addBehaviour(new Register());
		this.addBehaviour(new Request());
		this.addBehaviour(new Reply());
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
					System.out.println("Começando um pedido de aluguer...");
					Random rand = new Random();

					int origem = rand.nextInt(estacoes.size());
					int destino = rand.nextInt(estacoes.size());
					while(origem == destino){
						destino = rand.nextInt(estacoes.size());
					}

					init = estacoes.get(origem);
					dest = estacoes.get(destino);

					MakeRequest mr = new MakeRequest(myAgent.getAID(), init, dest);
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
							System.out.println("-- result[i].getName() = " + result[i].getName());
						}
					}

					mensagem.setContentObject(mr);
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
				if(msg.getPerformative() == ACLMessage.CONFIRM) {
					// If where transportation was completed!
					myAgent.doDelete();
				} else {
					// If where no taxis were available!
					myAgent.doDelete();
				}
			} else {
				block();
			}
		}
	}
}
