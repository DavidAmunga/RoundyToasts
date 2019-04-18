package miles.identigate.soja.service.network.api;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import miles.identigate.soja.service.storage.model.Guest;

import static miles.identigate.soja.app.Common.GUEST_LIST;
import static miles.identigate.soja.app.Common.VISITORS_LIST;

public class APIJsonSerializer implements JsonDeserializer {
    private static final String TAG = "APIJsonSerializer";

    String type;

    public APIJsonSerializer(String type) {
        this.type = type;
    }

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ArrayList<Guest> guests = null;

        switch (type) {
            case GUEST_LIST:
                try {
                    JsonObject jsonObject = json.getAsJsonObject();


                    JsonArray guestsArray = jsonObject.getAsJsonArray("result_content");


                    guests = new ArrayList<>(guestsArray.size());

                    JsonObject resultDetail = jsonObject.getAsJsonObject("result_detail");

//                    Calculate Any More
                    Boolean hasMore = resultDetail.get("hasMore").getAsBoolean();

                    for (int i = 0; i < guestsArray.size(); i++) {
                        Guest guest = context.deserialize(guestsArray.get(i), Guest.class);
                        Log.d(TAG, "deserialize: " + guest.getFirstName());
                        guests.add(guest);
                    }
                } catch (JsonParseException e) {
                    Log.e(TAG, "Could Not Deserialize Appointments Element: ", e);
                }
                break;
            case VISITORS_LIST:

                break;
        }
        return guests;

    }


}
