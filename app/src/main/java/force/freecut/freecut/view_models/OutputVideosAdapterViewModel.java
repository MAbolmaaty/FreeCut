package force.freecut.freecut.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import force.freecut.freecut.adapters.OutputVideosAdapter;

public class OutputVideosAdapterViewModel extends ViewModel {

    private MutableLiveData<OutputVideosAdapter> mOutputVideosAdapter =
            new MutableLiveData<>();

    public void setOutputVideosAdapter(OutputVideosAdapter outputVideosAdapter){
        mOutputVideosAdapter.setValue(outputVideosAdapter);
    }

    public LiveData<OutputVideosAdapter> getOutputVideosAdapter(){
        return mOutputVideosAdapter;
    }
}
