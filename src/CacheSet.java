import java.util.*;

public class CacheSet
{
    private List<MyCacheBlock> myCacheBlockQueue = new ArrayList<>();
    private int J;                                                                  //No of blocks in a Set
    private int priority;

    public CacheSet(int J, MyCacheBlock myCacheBlock)
    {
        this.J = J;
        myCacheBlockQueue.add(myCacheBlock);
    }

    public List<MyCacheBlock> getQueue()
    {
        return myCacheBlockQueue;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public void AddCache(MyCacheBlock myCacheBlock)
    {
        if (myCacheBlockQueue.size() < J)
            myCacheBlockQueue.add(myCacheBlock);
        else
        {
            myCacheBlockQueue.remove(0);
            myCacheBlockQueue.add(myCacheBlock);
        }
    }
}