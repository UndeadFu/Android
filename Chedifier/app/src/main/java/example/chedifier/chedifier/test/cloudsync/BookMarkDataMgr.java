package example.chedifier.chedifier.test.cloudsync;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import example.chedifier.chedifier.test.cloudsync.framework.Patch;

/**
 * Created by Administrator on 2017/3/29.
 */

public class BookMarkDataMgr {

    private static final String TAG = "BookMarkDataMgr";

    public BookMarkDataMgr(){

    }

    private ArrayList<Bookmark> mData = new ArrayList<>();

    private Patch mPatchMgr = new Patch<Bookmark>();

    public ArrayList<Bookmark> getData(){
        return mData;
    }

    public Patch getPatch(){
        return mPatchMgr;
    }

    public void reorder(){
        ArrayList<Bookmark> ordered = getOrderedData(Bookmark.ID_ROOT);
        mData.clear();
        mData.addAll(ordered);
    }

    public ArrayList<Bookmark> getOrderedData(long bid){
        ArrayList<Bookmark> bookmarks = new ArrayList<>();
        Bookmark dir = getBookmarkByLuid(bid);
        if(dir != null){
            bookmarks.add(dir);


        }
        return bookmarks;
    }

    public Bookmark getBookmarkBySignature(String signature){
        for(Bookmark b:mData){
            if(b.getSignature().equals(signature)){
                return b;
            }
        }

        return null;
    }

    public Bookmark getBookmarkByLuid(long id){
        for(Bookmark b:mData){
            if(b.luid == id){
                return b;
            }
        }

        return null;
    }

    public void clear(){
        mData.clear();
        mPatchMgr.clear();
    }

    public void resetDirty(){
        mPatchMgr.clear();
    }

    public void copyFrom(BookMarkDataMgr dmgr, boolean includeDel){
        mData.clear();
        mPatchMgr.clear();

        ArrayList<Bookmark> data = dmgr.getData();
        if(data != null){
            for(int i=0;i<data.size();i++){
                if(!includeDel && data.get(i).getDirty() == Bookmark.DIRTY_TYPE.DELETE){
                    continue;
                }

                mData.add(data.get(i).syncClone());
            }
        }

        mPatchMgr = dmgr.getPatch().copy();
    }

    public boolean add(Bookmark data){
        return add(data,true);
    }

    public boolean add(Bookmark data,boolean patch){
        return addInDirectory(0,data,patch);
    }

    public boolean addInDirectory(int pos,Bookmark data,boolean patch){
        mData.add(data);

        if(patch){
            mPatchMgr.add(data);


        }

        return true;
    }

    public boolean cloudDelete(Bookmark data){
        return mData.remove(data);
    }

    public boolean delete(Bookmark data){
        mData.remove(data);
        data.updateDirtyType(Bookmark.DIRTY_TYPE.DELETE);

        // update directory order list

        mPatchMgr.delete(data);
        return true;
    }

    public void cleanupDirectoryOrderList(){

    }

    public boolean update(Bookmark data){

        Bookmark.DIRTY_TYPE type = data.getDirty();
        delete(data);
        add(data);
        data.updateDirtyType(type);

        return true;
    }

    //data have already changed!
    public boolean swapData(int p1,int p2){
        Log.i(TAG,"swap " + p1 + " " + p2);
        if(p1 == p2 || !(0<p1&&p1<mData.size()&&0<p2&&p2<mData.size())){
            return false;
        }

        Bookmark b = mData.get(p2);
        Bookmark dir = getBookmarkByLuid(b.p_luid);
        if(dir == null){
            return false;
        }

        return false;
    }

    public void logout(){
        String s = "";
        for(int i=0;i<mData.size();i++){
            s += String.valueOf(mData.get(i).luid) + " -> " + String.valueOf(mData.get(i).p_luid);
        }
        Log.i(TAG,s);
    }

    public String dumpJson(){

        String json = "";
        for(int i=0;i<mData.size();i++){
            json += mData.get(i).toJson().toString();
        }

        return json;
    }
}