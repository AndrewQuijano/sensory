package columbia.irt.sensory;

import android.os.Bundle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

public class CollectionFragment extends Fragment
{
    Switch start;
    Switch indoors;
    Switch center;
    NumberPicker floor;
    EditText env_context;
    EditText room;
    EditText building;
    String [] options;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.collection_settings, container, false);
        start = rootView.findViewById(R.id.start);
        indoors = rootView.findViewById(R.id.Indoors);
        center = rootView.findViewById(R.id.center);
        env_context = rootView.findViewById(R.id.env_context);
        room = rootView.findViewById(R.id.room);
        building = rootView.findViewById(R.id.building);
        floor = rootView.findViewById(R.id.floor);
        options = new String[20];
        for (int i = 0; i < 20; i++)
        {
            options[i] = String.valueOf(i + 1);
        }
        floor.setMinValue(1);
        floor.setMaxValue(options.length);
        floor.setDisplayedValues(options);

        // Inflate the layout for this fragment
        return rootView;
    }

    public void onViewCreated (View view,
                        Bundle savedInstanceState)
    {

    }
}