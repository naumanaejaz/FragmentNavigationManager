package com.naumanaejaz.fragmentnavigationmanager.lib;

import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by naumanaejaz on 2019-11-25.
 */
public class FragmentNavigationManager {

    public interface FragmentInterface {
        String defineUniqueKey();
        boolean onBackPressed();
        int getForwardEnterAnimation();
        int getForwardExitAnimation();
        int getBackwardEnterAnimation();
        int getBackwardExitAnimation();
        void onVisibleFromBackground();
        Fragment getFragment();
    }

    private final Stack<FragmentInterface> stack = new Stack<>();
    private final FragmentManager fragmentManager;
    private final int fragmentContainerId;
    private final boolean allowAnimations;

    public FragmentNavigationManager(FragmentManager fragmentManager, int fragmentContainerId, boolean allowAnimations) {
        this.fragmentManager = fragmentManager;
        this.fragmentContainerId = fragmentContainerId;
        this.allowAnimations = allowAnimations;
    }

    public void addFragmentWithNewStack(FragmentInterface fragment) {
        try {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            clearStack(fragmentTransaction);
            addFragment(fragmentTransaction, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Throwable e) {
            Log.w("FragmentNavigation", e);
        }
    }

    public void addFragment(FragmentInterface fragment) {
        try {
            if (skipAddingWhenSameOnTop(fragment)) {
                return;
            }

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            addFragment(fragmentTransaction, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Throwable e) {
            Log.w("FragmentNavigation", e);
        }
    }

    public void replaceFragment(FragmentInterface fragment) {
        try {
            if (skipAddingWhenSameOnTop(fragment)) {
                return;
            }

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            removeLastFragment(fragmentTransaction);
            addFragment(fragmentTransaction, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Throwable e) {
            Log.w("FragmentNavigation", e);
        }
    }

    public boolean onBackPressed() {
        if (stack.size() > 0 && stack.peek().onBackPressed()) {
            return true;
        }
        return removeLastFragment();
    }

    public boolean removeLastFragment() {
        if (stack.size() == 0) {
            return false;
        }
        try {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            setAnimations(stack.peek(), fragmentTransaction, false);
            boolean success = removeLastFragment(fragmentTransaction);
            stack.peek().onVisibleFromBackground();
            fragmentTransaction.commitAllowingStateLoss();
            return success;
        } catch (Throwable e) {
            Log.w("FragmentNavigation", e);
        }
        return false;
    }

    public void removeEverything() {
        try {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            clearStack(fragmentTransaction);
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Throwable e) {
            Log.w("FragmentNavigation", e);
        }
    }

    public void removeUntilRootFragment() {
        try {
            if (stack.size() > 0) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                while (stack.size() > 1) {
                    FragmentInterface fragmentInterface = stack.pop();
                    fragmentTransaction.detach(fragmentInterface.getFragment());
                    fragmentTransaction.remove(fragmentInterface.getFragment());
                    Log.d("FragmentNavigation", String.format("Removing fragment: %s", fragmentInterface.getFragment().toString()));
                }
                fragmentTransaction.commitAllowingStateLoss();
            }
        } catch (Throwable e) {
            Log.w("FragmentNavigation", e);
        }
    }

    public <T> ArrayList<FragmentInterface> getFragmentFromCurrentStack(Class<T> fragmentType) {
        ArrayList<FragmentInterface> arrayList = new ArrayList<>();
        try {
            if (stack.size() > 0) {
                for (FragmentInterface fragmentInterface : stack) {
                    if (fragmentInterface.getFragment().getClass() == fragmentType) {
                        arrayList.add(fragmentInterface);
                    }
                }
            }
        } catch (Throwable e) {
            Log.w("FragmentNavigation", e);
        }
        return arrayList;
    }

    private void clearStack(FragmentTransaction fragmentTransaction) {
        try {
            if (stack.size() > 0) {
                for (FragmentInterface fragmentInterface : stack) {
                    fragmentTransaction.detach(fragmentInterface.getFragment());
                    fragmentTransaction.remove(fragmentInterface.getFragment());
                }
                stack.clear();
            }
        } catch (Throwable e) {
            Log.w("FragmentNavigation", e);
        }
    }

    private void addFragment(FragmentTransaction fragmentTransaction, FragmentInterface fragment) {
        try {
            stack.push(fragment);
            setAnimations(fragment, fragmentTransaction, true);
            if (!TextUtils.isEmpty(fragment.defineUniqueKey())) {
                fragmentTransaction.add(fragmentContainerId, fragment.getFragment(), fragment.defineUniqueKey());
            } else {
                fragmentTransaction.add(fragmentContainerId, fragment.getFragment());
            }
            Log.d("FragmentNavigation", String.format("Fragment add: stack = %d, backStack = %d", stack.size(), fragmentManager.getBackStackEntryCount()));
        } catch (Throwable e) {
            Log.w("FragmentNavigation", e);
        }
    }

    private boolean removeLastFragment(FragmentTransaction fragmentTransaction) {
        try {
            if (stack.size() > 0) {
                Fragment fragmentToRemove = stack.pop().getFragment();
                fragmentTransaction.detach(fragmentToRemove);
                fragmentTransaction.remove(fragmentToRemove);
                Log.d("FragmentNavigation", String.format("Fragment remove: stack = %d, backStack = %d", stack.size(), fragmentManager.getBackStackEntryCount()));
                return true;
            }
        } catch (Throwable e) {
            Log.w("FragmentNavigation", e);
        }
        return false;
    }

    private boolean skipAddingWhenSameOnTop(FragmentInterface fragment) {
        String key = fragment.defineUniqueKey();
        if (!TextUtils.isEmpty(key) && stack.size() > 0 && stack.peek().defineUniqueKey().equals(key)) {
            Log.d("FragmentNavigation", String.format("Fragment adding skipped due to same key = %s", key));
            return true;
        }
        return false;
    }

    private void setAnimations(FragmentInterface fragment, FragmentTransaction fragmentTransaction, boolean isForward) {
        if (allowAnimations) {
            if (isForward) {
                fragmentTransaction.setCustomAnimations(fragment.getForwardEnterAnimation(), fragment.getForwardExitAnimation(), fragment.getForwardEnterAnimation(), fragment.getForwardExitAnimation());
            } else {
                fragmentTransaction.setCustomAnimations(fragment.getBackwardEnterAnimation(), fragment.getBackwardExitAnimation(), fragment.getBackwardEnterAnimation(), fragment.getBackwardExitAnimation());
            }
        }
    }
}
