package de.codekenner.roadtrip.Activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fima.cardsui.objects.Card;
import com.fima.cardsui.views.CardUI;

import de.codekenner.roadtrip.Cards.ImageCard;
import de.codekenner.roadtrip.R;
import de.codekenner.roadtrip.domain.Note;
import de.codekenner.roadtrip.domain.Trip;
import de.codekenner.roadtrip.storage.DataAccessException;
import de.codekenner.roadtrip.storage.RoadTripStorageService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotesFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class NotesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Trip trip;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotesFragment newInstance(String param1, String param2) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public NotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_notes, container, false);

        // Get selected trip data
        final long tripID = getArguments().getLong("id");
        try {
            this.trip = RoadTripStorageService.instance().getTrip(getActivity(), tripID);
        } catch (DataAccessException e) {
            Log.e("NotesFragment", e.getMessage());
        }

        ((TripsActivity) getActivity()).setActionBarTitle(this.trip.getName());

        CardUI cardsView = (CardUI) rootView.findViewById(R.id.cardsview);
        if (trip.getNotes().size() == 0) {
            // No Notes available for the trip
            Card newCard = new ImageCard(getString(R.string.notes_trip_empty), getString(R.string.notes_trip_empty_description), 0);

            newCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((TripsActivity) getActivity()).addNoteForTrip(tripID);
                }
            });

            cardsView.addCard(newCard);

        } else {
            // Add Cards for all the notes of the selected trip

            for (final Note note : this.trip.getNotes()) {
                final Card newCard = new ImageCard(note.getName(), note.getDate().toString(), 0);

                // Set onClickListener to switch to note details
                newCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //((TripsActivity) getActivity()).showNotesForTrip(trip.getId());
                    }
                });

                cardsView.addCard(newCard);
            }
        }
        cardsView.refresh();

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        public void onFragmentInteraction(Uri uri);
    }

}
