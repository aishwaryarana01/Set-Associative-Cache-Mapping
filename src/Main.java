public class Main
{
    public static void main(String[] args)
    {
        SetAssociativeCacheMgmtSimulation setAssociativeCacheMgmtSimulation;

        System.out.println("Simulation 1\nn:24 L:4 M:16 J:4");
        setAssociativeCacheMgmtSimulation = new SetAssociativeCacheMgmtSimulation(24,4, 16, 4, "Trace.txt", false);
        setAssociativeCacheMgmtSimulation.Simulate();
        System.out.println("\n");
        System.out.println("Simulation 2\nn:24 L:8 M:16 J:4");
        setAssociativeCacheMgmtSimulation = new SetAssociativeCacheMgmtSimulation(24,8, 16, 4, "Trace.txt", false);
        setAssociativeCacheMgmtSimulation.Simulate();
    }
}