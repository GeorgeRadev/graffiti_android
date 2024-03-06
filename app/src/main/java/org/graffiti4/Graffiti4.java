package org.graffiti4;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class Graffiti4 extends InputMethodService {

    private InputConnection inputConnection;

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
    public View onCreateCandidatesView() {
        return null;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        inputConnection = getCurrentInputConnection();
    }

    @Override
    public void onFinishInput() {
        inputConnection = null;
        super.onFinishInput();
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    InputConnection getInputConnection() {
        return inputConnection;
    }
}
