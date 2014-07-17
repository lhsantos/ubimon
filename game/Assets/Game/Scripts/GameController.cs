using System.Collections.Generic;
using UnityEngine;
using UOS;


[RequireComponent(typeof(uOS))]
public class GameController : MonoBehaviour, UOSApplication, Logger
{
    private const int TURN_MINE = 0;
    private const int TURN_ENEMY = 1;
    private const int TURN_PICK = -1;
    private const int TURN_CAPTURED = -2;
    private const int TURN_EXIT = -3;


    private enum Mode
    {
        World,
        Station,
        Battle
    }

    public static readonly Vector2 refResolution = new Vector2(1366, 768);

    public GUISkin skin;
    public GUISkin battleSkin;


    /// <summary>
    /// The singleton instance of this component.
    /// </summary>
    public static GameController main { get; private set; }

    /// <summary>
    /// The uOS gateway instance.
    /// </summary>
    public UnityGateway gateway { get; private set; }

    private Mode mode = Mode.World;
    private string playerId;
    private UbimonDatabase ubimonDB;
    private List<Ubimon> ubimons = new List<Ubimon>();
    private UbimonContainer ubimonContainer;
    private float battleTimer;
    private float battleCheckInterval = 7f;
    private GameObject battleBG;


    /// <summary>
    /// Called when this component is created in the scene.
    /// </summary>
    void Awake()
    {
        main = this;
        //playerId = System.Guid.NewGuid().ToString();
        playerId = "teste";
        ubimonDB = UbimonDatabase.instance;
        ubimonContainer = new UbimonContainer(
            0, Mathf.CeilToInt(0.2f * Screen.height),
            Screen.width, Mathf.FloorToInt(Screen.height / 2 + 280 - 0.2f * Screen.height));

        battleBG = transform.FindChild("BattleBG").gameObject;
        battleBG.renderer.enabled = false;

        ubimons = new List<Ubimon>() { RandomEnemy() };
        ubimonContainer.icons = ubimonContainer.Fit(ubimons);

        battleTimer = battleCheckInterval;
    }

    /// <summary>
    /// Called right before the first Update.
    /// </summary>
    void Start()
    {
        Screen.sleepTimeout = SleepTimeout.NeverSleep;
        uOS.Init(this, this);
    }

    /// <summary>
    /// Called once every frame.
    /// </summary>
    void Update()
    {
        if (Input.GetKeyDown(KeyCode.Escape))
        {
            Application.Quit();
            return;
        }

        if ((mode == Mode.World) && GoogleMapsDriver.main.valid && (ubimons.Count > 0))
        {
            battleTimer -= Time.deltaTime;
            if (battleTimer <= 0)
            {
                battleTimer = battleCheckInterval;
                if (Random.Range(0, 1000) > 700)
                    StartBattle();
            }
        }
    }

    private Ubimon enemy;
    private Ubimon battlingUbimon;
    private int turn;
    private int nextTurn;
    private string battleMessage;
    private string ubimonNickname;
    private void StartBattle()
    {
        mode = Mode.Battle;
        WorldMapController.main.HideMap();
        enemy = RandomEnemy();
        PrepareBattleBG();
        turn = TURN_PICK;
        nextTurn = 0;
        battleMessage = null;
        ubimonNickname = "";
    }

    private void PrepareBattleBG()
    {
        battleBG.renderer.enabled = true;
        battleBG.transform.localScale = new Vector3(Screen.width, Screen.height, 1);

        GameObject enemySprite = new GameObject("enemy", typeof(SpriteRenderer));
        Sprite s = enemy.prototype.sprite;
        enemySprite.GetComponent<SpriteRenderer>().sprite = s;
        enemySprite.transform.parent = transform;
        float scale = 0.2f * Screen.height / s.texture.height;
        enemySprite.transform.localScale = new Vector3(scale, scale, 1);
        enemySprite.transform.localRotation = Quaternion.identity;
        enemySprite.transform.localPosition =
            new Vector3(
                Screen.width / 2 - 40 - scale * s.texture.width / 2,
                Screen.height / 2 - 40 - scale * s.texture.height / 2,
                battleBG.transform.localPosition.z - 1
            );
    }

    private void ReleaseBattleBG()
    {
        Destroy(transform.FindChild("enemy").gameObject);
        battleBG.renderer.enabled = false;
    }

    private Ubimon RandomEnemy()
    {
        UbimonData data = ubimonDB.ubimons[Random.Range(0, ubimonDB.ubimons.Length)];
        Ubimon e = new Ubimon();
        e.prototype = data;
        e.id = System.Guid.NewGuid().ToString();
        e.trainer = playerId;
        e.name = char.ToUpper(data.name[0]) + data.name.ToLower().Substring(1);
        e.level = 1;
        Move m = new Move();
        m.minLevel = 1;
        m.name = "Tackle";
        e.moves = new List<Move>() { m };
        e.maxLife = 10;
        e.life = 10;

        return e;
    }

    void OnGUI()
    {
        float btnWidth = Mathf.Max(100, Screen.width * 0.2f);
        float btnHeight = 40;
        Vector2 center = new Vector2(Screen.width, Screen.height) / 2;

        GUI.skin = skin;
        switch (mode)
        {
            case Mode.World:
                WorldGUI(center, btnWidth, btnHeight);
                break;

            case Mode.Station:
                StationGUI(center, btnWidth, btnHeight);
                break;

            case Mode.Battle:
                BattleGUI(center, btnWidth, btnHeight);
                break;
        }
        GUI.skin = null;

        GlobalPosition p = WorldMapController.main.pos;
        string stats =
            string.Format("[{0:f6},{1:f6}]\u00B1{2:f6}", p.latitude, p.longitude, p.delta)
            + "\n" + MiniJSON.Json.Serialize(gateway.currentDevice.ToJSON());
        GUILayout.Label(stats);
    }

    private void WorldGUI(Vector2 center, float btnWidth, float btnHeight)
    {
        // Draws neighbours buttons.
        if (WorldMapController.main.neighbours != null)
        {
            var neighbours = WorldMapController.main.neighbours;
            var p = WorldMapController.main.pos;
            float width = Mathf.Max(100, Screen.width * 0.2f);
            Rect pos = new Rect(Screen.width - width - 20, 20, width, 40);
            int shown = 0, i = 0;
            while (i < neighbours.Count)
            {
                if ((neighbours[i].distance - neighbours[i].pos.delta - p.delta) < 50)
                {
                    if (GUI.Button(pos, neighbours[i].name))
                    {
                        Interact(neighbours[i]);
                        break;
                    }

                    shown++;
                    pos.y += 50;
                }
                ++i;
            }
        }
    }

    private void Interact(WorldEntity e)
    {
        if (e.type == WorldEntity.Type.Station)
        {
            WorldMapController.main.HideMap();
            mode = Mode.Station;
            station = e;
            stationDevice = null;
            reachingStation = false;
            stationError = null;
            stationGetting = false;
            stationSending = false;
            try
            {
                stationDevice = UpDevice.FromJSON(MiniJSON.Json.Deserialize(e.deviceDesc));
                reachingStation = true;
                (new System.Threading.Thread(StationReachThread)).Start();

            }
            catch (System.Exception ex)
            {
                reachingStation = false;
                stationError = ex.ToString();
            }
        }
    }

    private void StationGUI(Vector2 center, float btnWidth, float btnHeight)
    {
        bool stationOK = false;
        Rect r;

        r = new Rect(0.2f * Screen.width, 40, 0.6f * Screen.width, 0.3f * Screen.height);
        if (stationDevice == null)
            GUI.Label(r, "This station is currently unreacheable!");
        else if (stationError != null)
            GUI.Label(r, "Error while trying to reach the station:\n" + stationError);
        else if (reachingStation)
            GUI.Label(r, "Trying to reach station...");
        else
        {
            GUI.Label(r, station.name);
            stationOK = true;

            if (stationGetting)
                StationGetGUI(center, btnWidth, btnHeight);
            else if (stationSending)
                StationSendGUI(center, btnWidth, btnHeight);
            else
            {
                r = new Rect(center.x - 2 * btnWidth, center.y - btnHeight / 2, btnWidth, btnHeight);
                if (GUI.Button(r, "GET"))
                {
                    stationGetting = true;
                    StationPeek();
                }

                r = new Rect(center.x + btnWidth, center.y - btnHeight / 2, btnWidth, btnHeight);
                if (GUI.Button(r, "SEND"))
                    stationSending = true;
            }
        }

        r = new Rect(Screen.width - btnWidth - 20, center.y + 6.5f * btnHeight, btnWidth, btnHeight);
        if (GUI.Button(r, "BACK"))
        {
            if (stationGetting || stationSending)
                stationGetting = stationSending = false;
            else
            {
                if (stationOK)
                    StationCommand("leave");
                WorldMapController.main.ShowMap();
                mode = Mode.World;
                battleTimer = battleCheckInterval;
            }
        }
    }

    private void StationGetGUI(Vector2 center, float btnWidth, float btnHeight)
    {
        Rect r;
        r = new Rect(20, center.y - btnHeight / 2, btnWidth, btnHeight);
        if (GUI.Button(r, "LEFT"))
            StationCommand("cursorToLeft");

        r = new Rect(Screen.width - btnWidth - 20, center.y - btnHeight / 2, btnWidth, btnHeight);
        if (GUI.Button(r, "RIGHT"))
            StationCommand("cursorToRight");

        r = new Rect(center.x - btnWidth / 2, center.y - 4 * btnHeight, btnWidth, btnHeight);
        if (GUI.Button(r, "UP"))
            StationCommand("cursorToUp");

        r = new Rect(center.x - btnWidth / 2, center.y + 4 * btnHeight, btnWidth, btnHeight);
        if (GUI.Button(r, "DOWN"))
            StationCommand("cursorToDown");

        GUI.enabled = (newUbimonIcons != null);
        r = new Rect(center.x - btnWidth / 2, center.y - btnHeight / 2, btnWidth, btnHeight);
        if (GUI.Button(r, "GET"))
            StationGet();
        GUI.enabled = true;
    }

    private void StationSendGUI(Vector2 center, float btnWidth, float btnHeight)
    {
        ubimonContainer.OnGUI();

        Rect r = new Rect(20, center.y + 6.5f * btnHeight, btnWidth, btnHeight);
        GUI.enabled = (ubimonContainer.selected != null);
        if (GUI.Button(r, "SEND"))
            StationSend();
        GUI.enabled = true;
    }

    private WorldEntity station;
    private UpDevice stationDevice;
    private bool reachingStation;
    private string stationError;
    private bool stationGetting;
    private bool stationSending;
    private Ubimon peekedUbimon;
    private List<UbimonIcon> newUbimonIcons;
    private void StationReachThread()
    {
        while ((mode == Mode.Station) && (stationError == null) && reachingStation)
        {
            Call call = new Call("app", "enter", "ubimon");
            call.AddParameter("playerId", playerId);
            try
            {
                Response r = gateway.CallService(stationDevice, call);
                reachingStation = false;
                if ((r == null) || (!string.IsNullOrEmpty(r.GetResponseString("error"))))
                    stationError = (r == null) ? "No response!" : r.GetResponseString("error");
            }
            catch (System.Exception e)
            {
                reachingStation = false;
                stationError = e.ToString();
            }
        }
    }

    private void StationCommand(string command)
    {
        peekedUbimon = null;
        newUbimonIcons = null;
        Call call = new Call("app", command, "ubimon");
        call.AddParameter("playerId", playerId);
        gateway.CallService(stationDevice, call);

        if (command.Contains("cursor"))
            StationPeek();
    }

    private void StationPeek()
    {
        peekedUbimon = null;
        newUbimonIcons = null;
        Call call = new Call("app", "peek", "ubimon");
        call.AddParameter("playerId", playerId);
        Response r = gateway.CallService(stationDevice, call);
        string ubimon = r.GetResponseString("ubimon");
        if (ubimon != null)
        {
            try
            {
                peekedUbimon = Ubimon.FromJSON(ubimon, ubimonDB);
                newUbimonIcons = ubimonContainer.Fit(ubimons, peekedUbimon);
            }
            catch (System.Exception e)
            {
                StationCommand("leave");
                stationError = e.ToString();
            }
        }
    }

    private void StationGet()
    {
        ubimons.Add(peekedUbimon);
        ubimonContainer.icons = newUbimonIcons;
        StationCommand("removeSelected");
        StationPeek();
    }

    private void StationSend()
    {
        try
        {
            Call call = new Call("app", "store", "ubimon");
            call.AddParameter("playerId", playerId);
            call.AddParameter("ubimon", ubimonContainer.selected.ToString());
            Response r = gateway.CallService(stationDevice, call);
            if ((r != null) && string.IsNullOrEmpty(r.GetResponseString("error")))
            {
                ubimons.RemoveAll(u => u.id.Equals(ubimonContainer.selected.id));
                ubimonContainer.icons = ubimonContainer.Fit(ubimons);
            }
            else
                stationError = (r == null) ? "No response!" : r.GetResponseString("error");
        }
        catch (System.Exception e)
        {
            StationCommand("leave");
            stationError = e.ToString();
        }
    }


    private void BattleGUI(Vector2 center, float btnWidth, float btnHeight)
    {
        Rect r;
        Transform enemySprite = transform.FindChild("enemy");
        GUI.skin = battleSkin;
        Rect labelRect = new Rect(20, 20, Screen.width - enemySprite.localPosition.x - 20, 0.2f * Screen.height);
        Rect msgRect = new Rect(20, 0.7f * Screen.height, Screen.width - 40, 0.3f * Screen.height - 20);

        // Draws battle statuses...
        if (turn >= 0)
        {
            GUI.Label(labelRect, enemy.name + "\n" + "Lvl. " + enemy.level + "\n" + enemy.life + "/" + enemy.maxLife);

            GUIStyle style = new GUIStyle(battleSkin.label);
            style.alignment = TextAnchor.LowerRight;
            r = new Rect(enemySprite.localPosition.x - 20, 0.5f * Screen.height, labelRect.width, labelRect.height);
            GUI.Label(r,
                battlingUbimon.name + "\n" +
                "Lvl. " + battlingUbimon.level + "\n" +
                battlingUbimon.life + "/" + battlingUbimon.maxLife, style);
        }


        // Is there a pending message?
        if (battleMessage != null)
        {
            if (GUI.Button(msgRect, battleMessage))
            {
                if ((turn != TURN_PICK) || battleMessage.Contains("selected"))
                    turn = nextTurn;
                battleMessage = null;

                if (turn >= 0)
                {
                    nextTurn = 1 - turn;
                    if (turn == TURN_ENEMY)
                        EnemyAI();
                }
                // Go back to world mode!
                else if (turn == TURN_EXIT)
                {
                    ReleaseBattleBG();
                    WorldMapController.main.ShowMap();
                    mode = Mode.World;

                    foreach (var u in ubimons)
                        u.life = u.maxLife;
                    ubimonContainer.icons = ubimonContainer.Fit(ubimons);
                }
            }
        }
        else
        {
            switch (turn)
            {
                // Did I get a ubimon?
                case TURN_CAPTURED:
                    GUI.Label(labelRect, "Do you want to give a nickname to your new ubimon?");

                    float w = 0.3f * Screen.width;
                    float h = 30;
                    r = new Rect(center.x - w / 2, center.y - h / 2, w, h);
                    ubimonNickname = (GUI.TextField(r, ubimonNickname) ?? "").Trim();

                    GUI.enabled = !string.IsNullOrEmpty(ubimonNickname);
                    r = new Rect(center.x - btnWidth - 10, center.y + 2 * btnHeight, btnWidth, btnHeight);
                    if (GUI.Button(r, "OK"))
                    {
                        enemy.name = ubimonNickname;
                        EnemyCaptured();
                    }
                    GUI.enabled = true;

                    r = new Rect(center.x + 10, center.y + 2 * btnHeight, btnWidth, btnHeight);
                    if (GUI.Button(r, "NO"))
                        EnemyCaptured();
                    break;


                // Is it in picking mode?
                case TURN_PICK:
                    GUI.Label(labelRect, "A wild " + enemy.prototype.name + " appeared... You must pick a ubimon to use!");

                    ubimonContainer.OnGUI();

                    r = new Rect(20, center.y + 6.5f * btnHeight, btnWidth, btnHeight);
                    GUI.enabled = (ubimonContainer.selected != null);
                    if (GUI.Button(r, "USE"))
                    {
                        battlingUbimon = ubimonContainer.selected;
                        battleMessage = "You selected " + battlingUbimon.name + "!";
                    }
                    GUI.enabled = true;
                    break;


                // Is it my turn?
                case TURN_MINE:
                    r = new Rect(20, 0.7f * Screen.height, 0.5f * Screen.width - 30, 0.15f * Screen.height - 15);
                    if (GUI.Button(r, "ATTACK"))
                        BattleAttack();

                    r = new Rect(center.x + 5, 0.7f * Screen.height, 0.5f * Screen.width - 30, 0.15f * Screen.height - 15);
                    if (GUI.Button(r, "AURABALL"))
                        BattleAuraBall();

                    r = new Rect(20, 0.85f * Screen.height + 5, 0.5f * Screen.width - 30, 0.15f * Screen.height - 15);
                    if (GUI.Button(r, "SWITCH"))
                        turn = TURN_PICK;

                    r = new Rect(center.x + 5, 0.85f * Screen.height + 5, 0.5f * Screen.width - 30, 0.15f * Screen.height - 15);
                    if (GUI.Button(r, "RUN"))
                        BattleRun();
                    break;

                default:
                    break;
            }
        }
    }

    private void BattleAttack()
    {
        float damage = battlingUbimon.maxLife * 1.5f / 10f;
        float defense = enemy.maxLife / 20f;
        damage = Mathf.Max(damage - defense, 1);
        int intDamage = Mathf.CeilToInt(damage);
        enemy.life -= intDamage;
        battleMessage = battlingUbimon.name + " attacked you and took " + intDamage + "!";

        if (enemy.life <= 0)
        {
            nextTurn = TURN_EXIT;
            battleMessage += " The wild " + enemy.prototype.name + " fainted! You won this round!";
        }
    }

    private void BattleAuraBall()
    {
        float p = Mathf.Max(enemy.maxLife - enemy.life, 1f) / enemy.maxLife;
        if (Random.Range(0f, 1f) <= p)
        {
            battleMessage = "Yes! You caught " + enemy.prototype.name + "!";
            nextTurn = TURN_CAPTURED;
        }
        else
            battleMessage = "Oh noes! It was so close, but he got away!";
    }

    private void BattleRun()
    {
        float p = (enemy.maxLife - 0.5f * enemy.life) / enemy.maxLife;
        if (Random.Range(0f, 1f) <= p)
        {
            battleMessage = "You got away safely!";
            nextTurn = TURN_EXIT;
        }
        else
            battleMessage = "You couldn't get away!";
    }

    private void EnemyAI()
    {
        Debug.Log("Enemy");
        float damage = enemy.maxLife / 10f;
        float defense = battlingUbimon.maxLife / 20f;
        damage = Mathf.Max(damage - defense, 1);
        int intDamage = Mathf.CeilToInt(damage);
        battlingUbimon.life -= intDamage;
        battleMessage = "Wild " + enemy.prototype.name + " attacked you and took " + intDamage + "!";

        if (battlingUbimon.life <= 0)
        {
            battleMessage += " " + battlingUbimon.name + " fainted! ";

            List<Ubimon> available = ubimons.FindAll(u => u.life > 0);
            if (available.Count > 0)
            {
                ubimonContainer.icons = ubimonContainer.Fit(available);
                turn = TURN_PICK;
                battleMessage += "You have to pick another ubimon!";
            }
            else
            {
                nextTurn = TURN_EXIT;
                battleMessage += "You have no more ubimon to battle... You ran away like a chicken!";
            }
        }
    }

    private void EnemyCaptured()
    {
        nextTurn = TURN_EXIT;
        var newIcons = ubimonContainer.Fit(ubimons, enemy);
        if (newIcons == null)
            battleMessage = "You can't hold any more ubimon, you do not have enough space!";
        else
        {
            ubimons.Add(enemy);
            ubimonContainer.icons = newIcons;
            battleMessage = "Your new ubimon was stored in the container!";
        }
    }


    #region uOS Interfaces
    void UOSApplication.Init(IGateway gateway, uOSSettings settings)
    {
        this.gateway = (UnityGateway)gateway;

        this.gateway.Register(
            WorldMapController.main,
            gateway.currentDevice,
            GlobalPositionDriver.DRIVER_ID, null, GlobalPositionDriver.EVENT_POS_CHANGE);

        WorldMapController.main.Init(gateway, settings);
    }

    void UOSApplication.TearDown()
    {
        this.gateway = null;
    }

    public void Log(object message)
    {
        DoLog("INFO:" + message.ToString());
    }

    public void LogError(object message)
    {
        DoLog("ERROR: " + message);
    }

    public void LogException(System.Exception e)
    {
        DoLog("ERROR: " + e.ToString());
    }

    public void LogWarning(object message)
    {
        DoLog("WARNING: " + message);
    }

    public static void DoLog(string msg)
    {
        //Debug.Log(msg);
    }
    #endregion
}
