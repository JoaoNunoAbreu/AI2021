package Agents;
import Util.InfoEstacao;
import Util.InfoUtilizador;
import Util.Mapa;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
				try {
					if (msg.getPerformative() == ACLMessage.SUBSCRIBE) {
						if(msg.getSender().getLocalName().contains("Estacao")){
							System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName() + " registered!");
							mapa.addNewEstacao((InfoEstacao) msg.getContentObject());
						}
						else if(msg.getSender().getLocalName().contains("Utilizador")){
							System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName() + " registered!");
							mapa.addUtilizadores((InfoUtilizador) msg.getContentObject());
							mapa.decrementaBicicleta((InfoUtilizador) msg.getContentObject());
						}
						else{
							System.out.println("Chegou um subscribe inválido ao Central!");
						}
					}
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
						else{
							System.out.println(myAgent.getAID().getLocalName() + ": " + msg.getSender().getLocalName() + " foi removido maybeeeerererer!");
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
}