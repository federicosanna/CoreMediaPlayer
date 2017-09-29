
/*
 * Written by Federico Sanna (federico.sanna15@ic.ac.uk)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.whatsnext.coremediaplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class InstrumentSelection extends Activity {
    HashMap<String, Integer> instruments = new HashMap<>();
    public final static String EXTRA_INSTRUMENT="EXTRA_INSTRUMENT";

    //Drums collection
    private final static String DRUMS_BASE ="Drums base";
    private final static String ACTIVE_BATTERY ="Active battery";
    private final static String CLASSICAL_PERCUSSION ="Classical percussion";
    private final static String DANCE_BASE ="Dance base";
    private final static String DISCO_FUNKY ="Disco funky";
    private final static String DODGY ="Dodgy";
    private final static String HIP_HOP_BASA ="Hip hop base";
    private final static String JAZZ_PALO_ALTO ="Jazz Palo Alto";
    private final static String JAZZY_BASE ="Jazzy base";
    private final static String METAL_BASE ="Metal base";
    private final static String ROCK_BASE ="Rock base";
    private final static String SALSA_BASE ="Salsa base";

    //Bass collection
    private final static String BASS_BASE ="Bass base";
    private final static String BASS_AND_PAD ="Bass and Pad";
    private final static String BRONXIO ="Bronxio";
    private final static String GROOVE ="Groove";
    private final static String LUCKY_LITTLE_RAVEN ="Lucky Little Raven";
    private final static String PARANOYE ="Paranoye";
    private final static String RESONANCE ="Resonance";
    private final static String LOOP_ROCK ="Loop Rock";
    private final static String SIMPLE_LOOP ="Simple loop";
    private final static String SURGEON_REKER ="Surgeon Reker";

    private final static String BRASILIAN_PERCUSSION ="Brasilian percussion";
    private final static String FUNK_GUITAR ="Funk guitar";
    private final static String GUITAR_FIVE_NOTES ="Guitar five notes";
    private final static String SING_ALONG ="Sing along!";
    private final static String SOLO_TRUMPET ="Solo trumpet";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_selection);
        Bundle displayTracks = getIntent().getExtras();
        int toDisplayTracks  = 0; // or other values
        if(displayTracks != null)
            toDisplayTracks = displayTracks.getInt("keyDisplayTracks");

        if (toDisplayTracks  == 0){
            instruments.put(BASS_BASE, R.raw.bass_loop);
            instruments.put(BASS_AND_PAD, R.raw.bass_and_pad);
            instruments.put(BRONXIO, R.raw.bass_bronxio);
            instruments.put(GROOVE, R.raw.bass_groove);
            instruments.put(LUCKY_LITTLE_RAVEN, R.raw.bass_lucky_little_raven);
            instruments.put(PARANOYE, R.raw.bass_paranoye);
            instruments.put(RESONANCE, R.raw.bass_resonance);
            instruments.put(LOOP_ROCK, R.raw.bass_rock_loop);
            instruments.put(SIMPLE_LOOP, R.raw.bass_simple_loop);
            instruments.put(SURGEON_REKER, R.raw.bass_surgeon_reker);
        } else if(toDisplayTracks  == 1) {
            instruments.put(DRUMS_BASE, R.raw.base_batteria);
            instruments.put(ACTIVE_BATTERY, R.raw.loop_active_battery);
            instruments.put(CLASSICAL_PERCUSSION, R.raw.loop_classical_percussion);
            instruments.put(DANCE_BASE, R.raw.loop_dance);
            instruments.put(DISCO_FUNKY, R.raw.loop_disco_funky);
            instruments.put(DODGY, R.raw.loop_dodgy);
            instruments.put(HIP_HOP_BASA, R.raw.loop_hiphop_drums);
            instruments.put(JAZZ_PALO_ALTO, R.raw.loop_jazz_palo_alto);
            instruments.put(JAZZY_BASE, R.raw.loop_jazzy);
            instruments.put(METAL_BASE, R.raw.loop_metal);
            instruments.put(ROCK_BASE, R.raw.loop_rock);
            instruments.put(SALSA_BASE, R.raw.loop_salsa);
        } else if(toDisplayTracks  == 2) {
            instruments.put(BRASILIAN_PERCUSSION, R.raw.brasilian_percussion);
            instruments.put(FUNK_GUITAR, R.raw.funk_guitar);
            instruments.put(GUITAR_FIVE_NOTES, R.raw.guitar_five_notes);
            instruments.put(SING_ALONG, R.raw.sing_along);
            instruments.put(SOLO_TRUMPET, R.raw.solo_trumpet);
        } else {
            instruments.put(BRASILIAN_PERCUSSION, R.raw.brasilian_percussion);
            instruments.put(FUNK_GUITAR, R.raw.funk_guitar);
            instruments.put(GUITAR_FIVE_NOTES, R.raw.guitar_five_notes);
            instruments.put(SING_ALONG, R.raw.sing_along);
            instruments.put(SOLO_TRUMPET, R.raw.solo_trumpet);
        }



        final ListView listview = (ListView) findViewById(R.id.listview);
        final String[] instrument = new String[instruments.size()];// { instruments.toString().substring(0, indexOf()) };

        //populate the Array instrument with the names of the instruments in the Hashmap
        int f = 0;
        int n = 0;
        do {
            instrument[n] = instruments.toString().substring(f + 1, f = instruments.toString().indexOf('=', f));
            n++;
        } while ((f = (instruments.toString().indexOf(',', f)) + 1)!= 0);

        //instrument[0] = instruments.toString().substring(1, instruments.toString().indexOf(','));

        final ArrayList<String> list = new ArrayList<String>();
        //list.add(instruments.toString());
        for (int i = 0; i < instrument.length; ++i) {
            list.add(instrument[i]);
        }
        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);

                Bundle b = new Bundle();
                b.putInt(EXTRA_INSTRUMENT, instruments.get(item));
                Intent result = new Intent();
                result.putExtras(b);
                setResult(Activity.RESULT_OK, result);
                finish();
            }

        });
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}
