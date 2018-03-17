using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SendSignal : ScriptableObject
{
    public string type;
    public bool triggered;
    public string id;
    public bool opened;
    public int scale;

    public void init(string typeInput, string idInput, bool triggeredInput)
    {
        type = typeInput;
        id = idInput;
        triggered = triggeredInput;
        scale = 0;
        opened = false;
    }
    public void init(string typeInput, bool openedInput)
    {
        type = typeInput;
        opened = openedInput;
        id = "0.0";
        triggered = false;
        scale = 0;
    }
}