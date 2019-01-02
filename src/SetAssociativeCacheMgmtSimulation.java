import java.io.File;
import java.math.BigInteger;
import java.util.*;

public class SetAssociativeCacheMgmtSimulation
{
    private double mainMemorySize; 			    //i.e. N
    private int mainMemoryBlockSize; 		    //i.e. M
    private int cacheBlockCount; 			    //i.e. L i.e. no of Sets
    private int noOfBlocksInASet;               //i.e. J i.e. No of blocks in a Set
    private String referenceWordsFileName;

    private double indexLength;
    private double offsetLength;
    private double tagLength;

    private int dirtyMissCount;
    private int hitCount;
    private int missCount;
    private int compulsoryMissCount;

    private boolean IS_DEBUG = false;

    private List<CacheSet> cacheSetQueue = new ArrayList<>();

    public SetAssociativeCacheMgmtSimulation(int n, int L, int M, int J, String referenceWordsFileName, boolean IS_DEBUG)
    {
        this.mainMemorySize = Math.pow(2, n);
        this.mainMemoryBlockSize = M;
        this.cacheBlockCount = L;
        this.noOfBlocksInASet = J;
        this.referenceWordsFileName = referenceWordsFileName;

        this.offsetLength = Math.ceil(logOfBase(2, this.mainMemoryBlockSize));
        this.indexLength = Math.ceil(logOfBase(2, this.cacheBlockCount));
        this.tagLength = getRefWordLength(this.referenceWordsFileName) - this.offsetLength - this.indexLength;

        this.IS_DEBUG = IS_DEBUG;
    }

    private String getDisplayBinaryString(String binaryVal)
    {
        int dashCount = 0;
        int x = 4;
        try
        {
            while (dashCount < 5)
            {
                binaryVal = binaryVal.substring(0, x) + "-" + binaryVal.substring(x);
                dashCount++;
                x += 5;
            }
        }
        catch (Exception ex)
        {

        }
        return binaryVal;
    }

    private double getRefWordLength(String referenceWordsFileName)
    {
        try
        {
            File fileToRead = new File(referenceWordsFileName);
            Scanner sc = new Scanner(fileToRead);

            while (sc.hasNextLine())
            {
                int len = getBinaryString(sc.nextLine()).length();
                sc.close();
                return len;
            }
        }
        catch (Exception ex)
        {
        }
        return 0;
    }

    private double logOfBase(int base, int num)
    {
        return Math.log(num) / Math.log(base);
    }

    private String PadFront(int totalSize, String str, String padCharacter)
    {
        int initialLength = str.length();
        for (int i = 0; i < totalSize - initialLength; i++)
            str = padCharacter + str;
        return str;
    }

    private String getBinaryString(String hexValue)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < hexValue.length(); i++)
        {
            String binary = new BigInteger(hexValue.substring(i, i+1), 16).toString(2);
            stringBuilder.append(PadFront(4, binary, "0"));
        }
        return stringBuilder.toString();
    }

    private int getIndexOfCacheSet(String setName)
    {
        for(int i = 0; i < cacheSetQueue.size(); i++)
        {
            String setNameAtGivenPosition = cacheSetQueue.get(i).getQueue().get(0).getIndex();
            if (setName.equals(setNameAtGivenPosition))
                return i;
        }
        return -1;
    }

    private boolean IsHit(MyCacheBlock myCacheBlockToCheck, boolean is_write)
    {
        int index = getIndexOfCacheSet(myCacheBlockToCheck.getIndex());

        if (index != -1)
        {
            CacheSet cacheSet = cacheSetQueue.get(index);
            List<MyCacheBlock> cacheSetBlocks = cacheSet.getQueue();

            for (int i = 0; i < cacheSetBlocks.size(); i++)
            {
                if (cacheSetBlocks.get(i).equals(myCacheBlockToCheck))
                {
                    //Update Priority... item at start of list is of least priority and item at last of list is one with highest priority i.e. last recently used at last
                    MyCacheBlock removed = cacheSetBlocks.remove(i); // remove last recently used from list
                    cacheSetBlocks.add(removed); // add the last recently used to the end / last of list

                    //update to dirty for write case
                    if (is_write)
                        removed.setDirty(true);

                    return true;
                }
            }

            cacheSetQueue.remove(index);
            cacheSetQueue.add(cacheSet);
        }
        return false;
    }

    private boolean IsCompulsoryMiss(MyCacheBlock myCacheBlockToCheck)
    {
        int index = getIndexOfCacheSet(myCacheBlockToCheck.getIndex());

        if (index == -1)
            return true;

        return false;
    }

    private int getMissIndex(MyCacheBlock myCacheBlockToCheck)
    {
        int index = getIndexOfCacheSet(myCacheBlockToCheck.getIndex());

        if (index != -1)
        {
            CacheSet cacheSet = cacheSetQueue.get(index);
            List<MyCacheBlock> cacheSetBlocks = cacheSet.getQueue();

            int i=0;
            for (MyCacheBlock myCacheBlock : cacheSetBlocks)
            {
                if (!myCacheBlock.getTag().equals(myCacheBlockToCheck.getTag()) && myCacheBlock.getIndex().equals(myCacheBlockToCheck.getIndex()))
                    return i;
                i++;
            }

            cacheSetQueue.remove(index);
            cacheSetQueue.add(cacheSet);
        }
        return -1;
    }

    public void Simulate()
    {
        try
        {
            File fileToRead = new File(this.referenceWordsFileName);
            Scanner sc = new Scanner(fileToRead);
            String inputReferenceWord;

            int counter=0;
            while (sc.hasNextLine())
            {
                counter++;
                inputReferenceWord = sc.nextLine();
                String initialHex = inputReferenceWord;
                inputReferenceWord = getBinaryString(inputReferenceWord);

                MyCacheBlock cacheToCheck = new MyCacheBlock();
                int tagIndex = (int) this.tagLength;
                int indexIndex = (int) this.tagLength + (int) this.indexLength;
                int offsetIndex = (int) this.tagLength + (int) this.indexLength + (int) this.offsetLength;

                cacheToCheck.setTag(inputReferenceWord.substring(0, tagIndex));
                cacheToCheck.setIndex(inputReferenceWord.substring(tagIndex, indexIndex));
                cacheToCheck.setOffset(inputReferenceWord.substring(indexIndex, offsetIndex));

                if(IsHit(cacheToCheck, counter % 2 == 0))
                {
                    if (IS_DEBUG)
                        System.out.println(String.format("Line %5d : ", counter) + initialHex +
                                " Hit           " + getDisplayBinaryString(inputReferenceWord) +
                                " Set Name : " + cacheToCheck.getIndex() +
                                " Tag :" + getDisplayBinaryString(cacheToCheck.getTag()));

                    hitCount++;
                }
                else if(IsCompulsoryMiss(cacheToCheck))
                {
                    if (IS_DEBUG)
                        System.out.println(String.format("Line %5d : ", counter) + initialHex +
                                " CM            " + getDisplayBinaryString(inputReferenceWord) +
                                " Set Name : " + cacheToCheck.getIndex() +
                                " Tag :" + getDisplayBinaryString(cacheToCheck.getTag()));

                    compulsoryMissCount++;

                    if (counter % 2 == 0)
                        cacheToCheck.setDirty(true);

                    //Remove the least Priority Set
                    if (cacheSetQueue.size() == this.cacheBlockCount)
                        cacheSetQueue.remove(0);

                    cacheSetQueue.add(new CacheSet(this.noOfBlocksInASet, cacheToCheck));
                }
                else if(getMissIndex(cacheToCheck) != -1)
                {
                    int cacheSetIndex = getIndexOfCacheSet(cacheToCheck.getIndex());

                    if (cacheSetQueue.get(cacheSetIndex).getQueue().size() == this.noOfBlocksInASet)
                    {
                        if (IS_DEBUG)
                            System.out.println(String.format("Line %5d : ", counter) + initialHex +
                                    " DIRTY MISS    " + getDisplayBinaryString(inputReferenceWord) +
                                    " Set Name : " + cacheToCheck.getIndex() +
                                    " Tag :" + getDisplayBinaryString(cacheToCheck.getTag()));

                        MyCacheBlock myCacheBlockToRemove = cacheSetQueue.get(cacheSetIndex).getQueue().remove(0);

                        if (myCacheBlockToRemove.isDirty())
                        {
                            //Write Back
                            dirtyMissCount++;
                        }
                        else
                        {
                            missCount++;
                        }
                    }
                    else
                    {
                        if (IS_DEBUG)
                            System.out.println(String.format("Line %5d : ", counter) + initialHex +
                                    " MISS          " + getDisplayBinaryString(inputReferenceWord) +
                                    " Set Name : " + cacheToCheck.getIndex() +
                                    " Tag :" + getDisplayBinaryString(cacheToCheck.getTag()));

                        missCount++;
                    }

                    //Write Case always write as dirty
                    if (counter % 2 == 0)
                        cacheToCheck.setDirty(true);

                    cacheSetQueue.get(cacheSetIndex).getQueue().add(cacheToCheck);
                }
                //Thread.sleep(200);
            }

            PrintStatistics();
        }
        catch (Exception ex)
        {
        }
    }

    private void PrintStatistics()
    {
        int totalRef = hitCount + compulsoryMissCount + missCount + dirtyMissCount;
        System.out.println("Hit Ratio        : " + ((double) hitCount / totalRef));
        System.out.println("CM Ratio         : " + ((double) compulsoryMissCount / totalRef));
        //System.out.println(" Miss Ratio       : " + ((double) missCount / totalRef);
        System.out.println("Dirty Miss Ratio : " + ((double) dirtyMissCount / totalRef));
        //System.out.println("Total Count      : "+ totalRef);
    }
}