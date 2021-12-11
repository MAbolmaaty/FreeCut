package force.freecut.freecut.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import force.freecut.freecut.ui.fragments.AboutFragment;
import force.freecut.freecut.ui.fragments.MergeFragment;
import force.freecut.freecut.ui.fragments.MyListFragment;
import force.freecut.freecut.ui.fragments.TrimFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
//            case 0:
//                return MyListFragment.newInstance(null, null);
            case 0:
                return AboutFragment.newInstance(null, null);
            case 1:
                return MergeFragment.newInstance(null, null);
            case 2:
                return TrimFragment.newInstance(null, null);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
