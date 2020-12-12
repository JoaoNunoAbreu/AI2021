
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * 
 */

/**
 * @author Filipe Goncalves
 *
 */
public class MainContainer {

	Runtime rt;
	ContainerController container;

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

	public void startAgentInPlatform(String name, String classpath, Object[] args) {
		try {
			AgentController ac = container.createNewAgent(name, classpath, args);
			ac.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MainContainer a = new MainContainer();

		try {
			a.initMainContainerInPlatform("localhost", "9885", "MainContainer");

			// Example of Container Creation (not the main container)
			// Create 3 different containers (separated environments) inside the
			// Main container

			Object[] args_input = new Object[] { "Container1", "Container2", "Container3" };

			ContainerController newcontainer0 = a.initContainerInPlatform("localhost", "9886", "Container0");
			ContainerController newcontainer1 = a.initContainerInPlatform("localhost", "9887",
					args_input[0].toString());
			ContainerController newcontainer2 = a.initContainerInPlatform("localhost", "9888",
					args_input[1].toString());
			ContainerController newcontainer3 = a.initContainerInPlatform("localhost", "9889",
					args_input[2].toString());

			// Start Seller1 and Customer1 in Container1
			// Start Seller2 and Customer2 in Container1
			// Start Seller3 and Customer3 in Container1

			AgentController seller1 = newcontainer1.createNewAgent("Seller1", "Agents.Seller", new Object[] {});
			AgentController seller2 = newcontainer2.createNewAgent("Seller2", "Agents.Seller", new Object[] {});
			AgentController seller3 = newcontainer3.createNewAgent("Seller3", "Agents.Seller", new Object[] {});

			AgentController customer1 = newcontainer1.createNewAgent("Customer1", "Agents.Customer", new Object[] {});
			AgentController customer2 = newcontainer2.createNewAgent("Customer2", "Agents.Customer", new Object[] {});
			AgentController customer3 = newcontainer3.createNewAgent("Customer3", "Agents.Customer", new Object[] {});

			seller1.start();
			seller2.start();
			seller3.start();

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			customer1.start();
			customer2.start();
			customer3.start();

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Start new Analyst Agent in Container1 every 10 seconds
			AgentController analyst = newcontainer0.createNewAgent("Analyst", "Agents.Analyst", args_input);// arguments
			analyst.start();

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

	}
}