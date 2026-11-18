package org.graffiti4;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class Graffiti4 extends InputMethodService {

    @Override
    public void onInitializeInterface() {
        super.onInitializeInterface();
    }

    @Override
    public View onCreateInputView() {
        Graffiti4View view = new Graffiti4View(this);
        view.setBottom(0);
        return view;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    InputConnection getInputConnection() {
        return getCurrentInputConnection();
    }
}
