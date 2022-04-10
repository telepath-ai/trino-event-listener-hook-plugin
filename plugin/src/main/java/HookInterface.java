import org.json.JSONObject;

interface Hook {
  public void sendHook(JSONObject payload);
}