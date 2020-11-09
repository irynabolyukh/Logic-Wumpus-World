import agents.LogisticAgent;
import wumpus.Agent;
import wumpus.World;

/**
 * Entry point for the application.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        try {
            // Create a 4x4 world
            World world = new World(4, 4);

            // Print the game title
            System.out.println("Hunt the Wumpus!");

            // Start and execute the AI agent
            Agent agent = new LogisticAgent(world.getWidth(), world.getHeight());
            world.execute(agent);

            // Print the board and score table
            System.out.println("Board:");
            System.out.println(world.renderAll());

            System.out.format("Results for %s:%n", world.getAgentName());
            System.out.println(world.renderScore());
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}
