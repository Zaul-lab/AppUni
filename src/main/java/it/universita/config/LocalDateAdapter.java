package it.universita.config;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;

    /**
     * Adatta LocalDate per Gson:
     * - in JSON lo rappresenta come stringa "yyyy-MM-dd"
     * - dal JSON ricostruisce il LocalDate a partire dalla stringa
     */
    public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            // Quando convertiamo Java -> JSON:
            // LocalDate(2000-09-22) diventa "2000-09-22"
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            // Quando convertiamo JSON -> Java:
            // "2000-09-22" torna a essere LocalDate.parse("2000-09-22")
            return LocalDate.parse(json.getAsString());
        }
    }

