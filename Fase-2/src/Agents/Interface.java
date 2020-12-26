package Agents;
import Util.InfoEstacao;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;

public class Interface extends Agent {

    private Mapa mapa;

    protected void setup() {
        super.setup();

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
        this.addBehaviour(new GenerateStatistics(this,5000));
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
                        clearConsole();
                        System.out.println(mapa.showMapa());
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

    private class GenerateStatistics extends TickerBehaviour {
        private DefaultCategoryDataset datasetEstacoes;
        private DefaultCategoryDataset datasetReqDev;
        private DefaultPieDataset datasetE;
        private ApplicationFrame window;

        public GenerateStatistics(Agent a, long period) {
            super(a, period);
            this.datasetEstacoes = new DefaultCategoryDataset();
            this.datasetReqDev = new DefaultCategoryDataset();
            this.datasetE = new DefaultPieDataset();
            this.window = new ApplicationFrame("Statistics");
        }

        @Override
        protected void onTick() {
            String time = String.valueOf((this.getPeriod()*this.getTickCount()) / 1000);

            for( int i=0; i <mapa.getEstacoes().size(); i++){
                datasetEstacoes.addValue(mapa.getNBicicletasPorEstacao(i), "Estação "+i, time);
            }
            JFreeChart estacao_chart = ChartFactory.createLineChart(
                    "Número de bicicletas",
                    "Tempo (s)",
                    "Número de bicicletas",
                    this.datasetEstacoes,
                    PlotOrientation.VERTICAL,
                    true,true,false);

            ChartPanel chartPanel_estacao_chart = new ChartPanel( estacao_chart );
            chartPanel_estacao_chart.setPreferredSize( new java.awt.Dimension( 460 , 267 ) );
            window.getContentPane().add(chartPanel_estacao_chart, BorderLayout.SOUTH);

            datasetE.clear();
            for(InfoEstacao ie : mapa.getEstacoes2()){
                datasetE.setValue(ie.getAgent().getLocalName(), ie.getNum_bicicletas());
            }
            JFreeChart estacoes_chart = ChartFactory.createPieChart(
                    "Percentagem de preenchimento",
                    this.datasetE,
                    true, true, false);
            ChartPanel chartPanel_estacoes_chart = new ChartPanel( estacoes_chart );

            chartPanel_estacoes_chart.setPreferredSize( new java.awt.Dimension( 460 , 267 ) );
            window.getContentPane().add(chartPanel_estacoes_chart, BorderLayout.EAST);

            for( int i=0; i <mapa.getEstacoes().size(); i++){
                datasetReqDev.addValue(mapa.getNRequisicoesPorEstacao(i),"Requisições", "Estaçao "+i);
                datasetReqDev.addValue(mapa.getNDevolucoesPorEstacao(i),"Devoluções", "Estaçao "+i);
            }

            JFreeChart barChart = ChartFactory.createBarChart(
                    "Número de requisições e devoluções",
                    "Estação",
                    "Quantidade",
                    this.datasetReqDev,
                    PlotOrientation.VERTICAL,
                    true, true, false
            );
            ChartPanel chartPanel = new ChartPanel( barChart);
            chartPanel.setPreferredSize(new java.awt.Dimension(360,267));
            window.getContentPane().add(chartPanel, BorderLayout.NORTH);

            this.window.setSize( 1024 , 768 );
            this.window.setVisible( true );
        }
    }

    public static void clearConsole(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
