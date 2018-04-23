using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.SceneManagement;

public class UIScript : MonoBehaviour {

    public InputField inputfield;
    public Toggle toggle;
    public GameObject button;

    bool check;

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

    public void SaveResults()
    {
        IP_Data.ipAddress = inputfield.text;
    }

    public void ToggleConnection()
    {
        IP_Data.connectToTCP = toggle.enabled;
    }

    public void StartSimulator()
    {
        SceneManager.LoadSceneAsync("what");
    }
}
