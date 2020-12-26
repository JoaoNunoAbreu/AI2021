package Agents;
import Util.IO;
import Util.InfoEstacao;
import Util.InfoUtilizador;
import Util.Mapa;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.List;

public class Central extends Agent {

	private Mapa mapa;
	private IO io;

	protected void setup() {
		super.setup();
		this.io = new IO();

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
		this.addBehaviour(new SendMap(this,50));
	}

	protected void takeDown() {
		super.takeDown();
	}

	private class Receiver extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				try {
					// Pedido de registos (Estação e Utilizador)
					if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {
						if(msg.getSender().getLocalName().contains("Estacao")){
							io.writeToLogs("*** " + msg.getSender().getLocalName() + " registado! ***");
							mapa.addNewEstacao((InfoEstacao) msg.getContentObject());
						}
						else if(msg.getSender().getLocalName().contains("Utilizador")){
							io.writeToLogs("*** " + msg.getSender().getLocalName() + " registado! ***");
							mapa.addUtilizadores((InfoUtilizador) msg.getContentObject());
							mapa.decrementaBicicleta((InfoUtilizador) msg.getContentObject());
						}
						else{
							io.writeToLogs(myAgent.getAID().getLocalName() + ": Subscribe inválido feito por" + msg.getSender().getLocalName());
						}
					}
					// Utilizador informa central da sua posição
					else if(msg.getPerformative() == ACLMessage.INFORM) {
						InfoUtilizador infoutilizador = (InfoUtilizador) msg.getContentObject();

						// O utilizador ainda não chegou ao destino
						if(!infoutilizador.getAtual().equals(infoutilizador.getDest())) {

							// Atualiza a posição do utilizador no mapa
							mapa.atualizaUtilizador((InfoUtilizador) msg.getContentObject());

							// Verifica se o utilizador ultrapassou os 3/4's do trajeto
							if(infoutilizador.distanciaPercorrida() >= infoutilizador.getDistance34()){
								List<InfoEstacao> estacoes = mapa.getEstacoesIn(infoutilizador.getAtual());

								// Informa as estações que têm o utilizador na sua APE
								ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
								mensagem.setContentObject(infoutilizador);
								for(InfoEstacao ie : estacoes) {
									if(!ie.getPosition().equals(infoutilizador.getInit()) && !ie.getPosition().equals(infoutilizador.getDest()))
										mensagem.addReceiver(ie.getAgent());
								}
								myAgent.send(mensagem);
							}
						}
						// Utilizador chega ao destino
						else{
							mapa.incrementaBicicleta((InfoUtilizador) msg.getContentObject());
						}
					}
				}
				catch (UnreadableException | IOException e) {
					e.printStackTrace();
				}
			}
			else block();
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------

	private class SendMap extends TickerBehaviour {

		public SendMap(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			try {
				sd.setType("interface");
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(myAgent, template);
				if (result.length > 0) {
					ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
					for (int i = 0; i < result.length; ++i) {
						mensagem.addReceiver(result[i].getName());
					}
					mensagem.setContentObject(mapa);
					myAgent.send(mensagem);
				}
			}
			catch (FIPAException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}