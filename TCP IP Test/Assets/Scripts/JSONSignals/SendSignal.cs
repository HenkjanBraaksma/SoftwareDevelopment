using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[Serializable]
public class LightSignal
{
    public string type;
    public bool triggered;
    public string id;

    public LightSignal (string typeInput, string idInput, bool triggeredInput)
    {
        type = typeInput;
        id = idInput;
        triggered = triggeredInput;
    }
}

[Serializable]
public class BridgeSignal
{
    public string type;
    public bool opened;

    public BridgeSignal(string typeInput, bool openedInput)
    {
        type = typeInput;
        opened = openedInput;
    }
}