import Util.Mapa;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MainContainer {

	private static Mapa mapa;
	private Runtime rt;
	private ContainerController container;

	public ContainerController initContainerInPlatform(String host, String port, String containerName) {
		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();

		// Create a Profile, where the launch arguments are stored
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		profile.setParameter(Profile.MAIN_HOST, host);
		profile.setParameter(Profile.MAIN_PORT, port);
		// create a non-main agent container
		ContainerController container = rt.createAgentContainer(profile);
		return container;
	}

	public void initMainContainerInPlatform(String host, String port, String containerName) {

		// Get the JADE runtime interface (singleton)
		this.rt = Runtime.instance();

		// Create a Profile, where the launch arguments are stored
		Profile prof = new ProfileImpl();

		prof.setParameter(Profile.MAIN_HOST, host);
		prof.setParameter(Profile.MAIN_PORT, port);
		prof.setParameter(Profile.CONTAINER_NAME, containerName);
		prof.setParameter(Profile.MAIN, "true");
		prof.setParameter(Profile.GUI, "true");

		// create a main agent container
		this.container = rt.createMainContainer(prof);
		rt.setCloseVM(true);
	}

	public void startAgentInPlatform(String name, String classpath) {
		try {
			AgentController ac = container.createNewAgent(name, classpath,  new Object[] {mapa});
			ac.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Mapa generateMap(){
		mapa = new Mapa(SimulationConfig.MAP_SIZE, SimulationConfig.NUM_ESTACOES, SimulationConfig.NUM_UTILIZADORES, SimulationConfig.NUM_MAX_BICICLETAS);
		mapa.geraPosEstacoes();
		return mapa;
	}

	public static void main(String[] args) {
		MainContainer a = new MainContainer();
		try {
			generateMap();
			a.initMainContainerInPlatform("localhost", "9885", "MainContainer");
			a.startAgentInPlatform("AgenteInterface", "Agents.Interface");
			a.startAgentInPlatform("AgenteCentral", "Agents.Central");

			for (int i = 0; i < SimulationConfig.NUM_ESTACOES; i++)
				a.startAgentInPlatform("AgenteEstacao " + i, "Agents.Estacao");
			for (int j = 0; j < SimulationConfig.NUM_UTILIZADORES; j++)
				a.startAgentInPlatform("AgenteUtilizador " + j, "Agents.Utilizador");

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("--------------------------------------------------------");
		System.out.println("--------------------------------------------------------");
		System.out.println("--------------------------------------------------------");
		System.out.println(mapa.toString());
		System.out.println("--------------------------------------------------------");
		System.out.println("--------------------------------------------------------");
		System.out.println("--------------------------------------------------------");

	}
}