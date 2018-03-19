package org.metafetish.buttplug.apps.exampleclientgui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.metafetish.buttplug.components.controls.ButtplugTabControl;
import org.metafetish.buttplug.server.IButtplugServerFactory;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExampleClientPanel.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExampleClientPanel#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExampleClientPanel extends Fragment {
    private static final String TAG = ExampleClientPanel.class.getSimpleName();

    private AppCompatActivity activity;
    private IButtplugServerFactory bpFactory;

    private SharedPreferences sharedPreferences;

    private OnFragmentInteractionListener listener;

    public ExampleClientPanel() {
        // Required empty public constructor
    }

    //TODO: Switch to getParentFragment()
    @SuppressLint("ValidFragment")
    public ExampleClientPanel(ButtplugTabControl bpTabControl) {
        this.bpFactory = bpTabControl;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ExampleClientPanel.
     */
    public static ExampleClientPanel newInstance() {
        return new ExampleClientPanel();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_example_client_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            this.activity = activity;
            this.sharedPreferences = this.activity.getPreferences(Context.MODE_PRIVATE);

            //TODO: Why doesn't this work?
            //this.bpFactory = (ButtplugTabControl) getParentFragment();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (this.listener != null) {
            this.listener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.listener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
