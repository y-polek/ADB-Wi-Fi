package dev.polek.adbwifi.model

import com.intellij.openapi.util.IconLoader
import dev.polek.adbwifi.ui.view.ColoredIcon
import javax.swing.Icon

data class CommandIcon(
    val id: String,
    val displayName: String,
    val tags: List<String>,
    val icon: Icon
)

@Suppress("LargeClass")
object CommandIconsProvider {

    private data class IconDefinition(
        val id: String,
        val displayName: String,
        val tags: List<String>
    )

    @Suppress("LongMethod")
    private fun iconDefinitions(): List<IconDefinition> = listOf(
        IconDefinition(
            "activity",
            "Activity",
            listOf(
                "pulse", "action", "motion", "movement", "exercise", "fitness", "healthcare",
                "heart rate monitor", "vital signs", "vitals", "emergency room", "er",
                "intensive care", "hospital", "defibrillator", "earthquake", "siesmic",
                "magnitude", "richter scale", "aftershock", "tremor", "shockwave", "audio",
                "waveform", "synthesizer", "synthesiser", "music"
            )
        ),
        IconDefinition("at-sign", "At Sign", listOf("mention", "at", "email", "message", "@")),
        IconDefinition(
            "ban",
            "Ban",
            listOf(
                "cancel", "no", "stop", "forbidden", "prohibited", "error", "incorrect", "mistake",
                "wrong", "failure", "circle", "slash", "null", "void"
            )
        ),
        IconDefinition(
            "barcode",
            "Barcode",
            listOf(
                "scan", "checkout", "till", "cart", "transaction", "purchase", "buy", "product",
                "packaging", "retail", "consumer"
            )
        ),
        IconDefinition(
            "battery",
            "Battery",
            listOf("power", "electricity", "energy", "accumulator", "charge")
        ),
        IconDefinition(
            "battery-charging",
            "Battery Charging",
            listOf("power", "electricity", "energy", "accumulator", "charge")
        ),
        IconDefinition(
            "battery-full",
            "Battery Full",
            listOf("power", "electricity", "energy", "accumulator", "charge")
        ),
        IconDefinition(
            "battery-low",
            "Battery Low",
            listOf("power", "electricity", "energy", "accumulator", "charge")
        ),
        IconDefinition(
            "battery-medium",
            "Battery Medium",
            listOf("power", "electricity", "energy", "accumulator", "charge")
        ),
        IconDefinition(
            "battery-plus",
            "Battery Plus",
            listOf(
                "power", "electricity", "energy", "accumulator", "charge", "plus", "economy",
                "health", "add", "new", "maximum", "upgrade", "extra", "+"
            )
        ),
        IconDefinition(
            "battery-warning",
            "Battery Warning",
            listOf("power", "electricity", "energy", "accumulator", "charge", "exclamation mark")
        ),
        IconDefinition("bell", "Bell", listOf("alarm", "notification", "sound", "reminder")),
        IconDefinition(
            "bell-off",
            "Bell Off",
            listOf("alarm", "notification", "silent", "reminder")
        ),
        IconDefinition(
            "bell-ring",
            "Bell Ring",
            listOf("alarm", "notification", "sound", "reminder")
        ),
        IconDefinition("bird", "Bird", listOf("peace", "freedom", "wing", "avian", "tweet")),
        IconDefinition("bluetooth", "Bluetooth", listOf("wireless")),
        IconDefinition("bluetooth-off", "Bluetooth Off", listOf("lost")),
        IconDefinition(
            "bookmark",
            "Bookmark",
            listOf(
                "save", "favorite", "mark", "label", "attachment", "file", "stick", "pin", "read",
                "clip", "marker", "tag"
            )
        ),
        IconDefinition("bookmark-minus", "Bookmark Minus", listOf("delete", "remove")),
        IconDefinition("bookmark-plus", "Bookmark Plus", listOf("add")),
        IconDefinition(
            "bug",
            "Bug",
            listOf(
                "issue", "error", "defect", "testing", "troubleshoot", "problem", "report",
                "debug", "code", "insect", "beetle"
            )
        ),
        IconDefinition(
            "bug-off",
            "Bug Off",
            listOf(
                "issue", "fixed", "resolved", "testing", "debug", "code", "insect", "kill",
                "exterminate", "pest control"
            )
        ),
        IconDefinition(
            "cable",
            "Cable",
            listOf(
                "cord", "wire", "connector", "connection", "link", "signal", "console", "computer",
                "equipment", "electricity", "energy", "electronics", "recharging", "charger",
                "power", "supply", "disconnected", "unplugged", "plugs", "interface", "input",
                "output", "audio video", "av", "rca", "scart", "tv", "television", "optical"
            )
        ),
        IconDefinition(
            "camera",
            "Camera",
            listOf(
                "photography", "lens", "focus", "capture", "shot", "visual", "image", "device",
                "equipment", "photo", "webcam", "video"
            )
        ),
        IconDefinition("camera-off", "Camera Off", listOf("photo", "webcam", "video")),
        IconDefinition(
            "card-sim",
            "SIM Card",
            listOf(
                "cellphone", "smartphone", "mobile", "network", "cellular", "service", "provider",
                "signal", "coverage", "disk", "data", "format", "storage", "flash", "digital",
                "contacts", "phone book", "contractual", "circuit board", "chip"
            )
        ),
        IconDefinition("check", "Check", listOf("done", "todo", "tick", "complete", "task")),
        IconDefinition(
            "check-check",
            "Check Check",
            listOf("done", "received", "double", "todo", "tick", "complete", "task")
        ),
        IconDefinition(
            "chevron-down",
            "Chevron Down",
            listOf("backwards", "reverse", "slow", "dropdown")
        ),
        IconDefinition(
            "chevron-left",
            "Chevron Left",
            listOf("back", "previous", "less than", "fewer", "menu", "<")
        ),
        IconDefinition(
            "chevron-right",
            "Chevron Right",
            listOf(
                "forward", "next", "more than", "greater", "menu", "code", "coding",
                "command line", "terminal", "prompt", "shell", ">"
            )
        ),
        IconDefinition(
            "chevron-up",
            "Chevron Up",
            listOf(
                "caret", "keyboard", "mac", "control", "ctrl", "superscript", "exponential",
                "power", "ahead", "fast", "^", "dropdown"
            )
        ),
        IconDefinition("chromium", "Chromium", listOf("browser", "logo")),
        IconDefinition("circle", "Circle", listOf("off", "zero", "record", "shape")),
        IconDefinition(
            "circle-check-big",
            "Circle Check",
            listOf("done", "todo", "tick", "complete", "task")
        ),
        IconDefinition(
            "circle-minus",
            "Circle Minus",
            listOf(
                "subtract", "remove", "decrease", "reduce", "calculate", "line", "operator",
                "code", "coding", "minimum", "downgrade", "-"
            )
        ),
        IconDefinition("circle-pause", "Circle Pause", listOf("music", "audio", "stop")),
        IconDefinition("circle-play", "Circle Play", listOf("music", "start", "run")),
        IconDefinition(
            "circle-plus",
            "Circle Plus",
            listOf(
                "add", "new", "increase", "increment", "positive", "calculate", "crosshair", "aim",
                "target", "scope", "sight", "reticule", "maximum", "upgrade", "extra", "operator",
                "join", "concatenate", "code", "coding", "+"
            )
        ),
        IconDefinition("circle-stop", "Circle Stop", listOf("media", "music")),
        IconDefinition("clipboard", "Clipboard", listOf("copy", "paste")),
        IconDefinition("clipboard-copy", "Clipboard Copy", listOf("copy", "paste")),
        IconDefinition("clipboard-paste", "Clipboard Paste", listOf("copy", "paste")),
        IconDefinition("clipboard-x", "Clipboard X", listOf("copy", "paste", "discard", "remove")),
        IconDefinition("clock", "Clock", listOf("time", "watch", "alarm")),
        IconDefinition("clock-check", "Clock Check", listOf("time", "watch", "alarm")),
        IconDefinition(
            "clock-plus",
            "Clock Plus",
            listOf("time", "watch", "alarm", "add", "create", "new")
        ),
        IconDefinition("cloud", "Cloud", listOf("weather")),
        IconDefinition("cloud-download", "Cloud Download", listOf("import")),
        IconDefinition("cloud-off", "Cloud Off", listOf("disconnect")),
        IconDefinition("cloud-upload", "Cloud Upload", listOf("file")),
        IconDefinition("command", "Command", listOf("keyboard", "key", "mac", "cmd", "button")),
        IconDefinition(
            "compass",
            "Compass",
            listOf("direction", "north", "east", "south", "west", "safari", "browser")
        ),
        IconDefinition("copy", "Copy", listOf("clone", "duplicate", "multiple")),
        IconDefinition(
            "copy-minus",
            "Copy Minus",
            listOf(
                "clone", "duplicate", "remove", "delete", "collapse", "subtract", "multiple", "-"
            )
        ),
        IconDefinition(
            "copy-plus",
            "Copy Plus",
            listOf("clone", "duplicate", "add", "multiple", "expand", "+")
        ),
        IconDefinition(
            "cpu",
            "CPU",
            listOf(
                "processor", "cores", "technology", "computer", "chip", "circuit", "memory", "ram",
                "specs", "gigahertz", "ghz"
            )
        ),
        IconDefinition("credit-card", "Credit Card", listOf("bank", "purchase", "payment", "cc")),
        IconDefinition("crop", "Crop", listOf("photo", "image")),
        IconDefinition(
            "database",
            "Database",
            listOf("storage", "memory", "container", "tin", "pot", "bytes", "servers")
        ),
        IconDefinition(
            "database-backup",
            "Database Backup",
            listOf(
                "storage", "memory", "bytes", "servers", "backup", "timemachine", "rotate",
                "arrow", "left"
            )
        ),
        IconDefinition(
            "database-search",
            "Database Search",
            listOf("storage", "memory", "container", "tin", "pot", "bytes", "servers")
        ),
        IconDefinition("delete", "Delete", listOf("backspace", "remove")),
        IconDefinition(
            "diff",
            "Diff",
            listOf("patch", "difference", "compare", "plus", "minus", "plus-minus", "math")
        ),
        IconDefinition("download", "Download", listOf("import", "export", "save")),
        IconDefinition(
            "eraser",
            "Eraser",
            listOf("pencil", "drawing", "undo", "delete", "clear", "trash", "remove")
        ),
        IconDefinition(
            "eye",
            "Eye",
            listOf(
                "view", "watch", "see", "show", "expose", "reveal", "display", "visible",
                "visibility", "vision", "preview", "read"
            )
        ),
        IconDefinition(
            "eye-off",
            "Eye Off",
            listOf(
                "view", "watch", "see", "hide", "conceal", "mask", "hidden", "visibility", "vision"
            )
        ),
        IconDefinition("fast-forward", "Fast Forward", listOf("music")),
        IconDefinition("file", "File", listOf("document")),
        IconDefinition("file-minus", "File Minus", listOf("delete", "remove", "erase", "document")),
        IconDefinition("file-plus", "File Plus", listOf("add", "create", "new", "document")),
        IconDefinition("files", "Files", listOf("multiple", "copy", "documents")),
        IconDefinition(
            "fingerprint-pattern",
            "Fingerprint Pattern",
            listOf("2fa", "authentication", "biometric", "identity", "security")
        ),
        IconDefinition(
            "flag",
            "Flag",
            listOf(
                "report", "marker", "notification", "warning", "milestone", "goal", "notice",
                "signal", "attention", "banner"
            )
        ),
        IconDefinition(
            "flag-off",
            "Flag Off",
            listOf(
                "unflag", "unmark", "report", "marker", "notification", "warning", "milestone",
                "goal", "notice", "signal", "attention", "banner"
            )
        ),
        IconDefinition(
            "flashlight",
            "Flashlight",
            listOf("torch", "light", "beam", "emergency", "safety", "tool", "bright")
        ),
        IconDefinition(
            "flashlight-off",
            "Flashlight Off",
            listOf("torch", "light", "beam", "emergency", "safety", "tool", "bright")
        ),
        IconDefinition("folder", "Folder", listOf("directory")),
        IconDefinition("folders", "Folders", listOf("multiple", "copy", "directories")),
        IconDefinition(
            "fullscreen",
            "Fullscreen",
            listOf("expand", "zoom", "preview", "focus", "camera", "lens", "image")
        ),
        IconDefinition("funnel", "Funnel", listOf("filter", "hopper")),
        IconDefinition(
            "funnel-plus",
            "Funnel Plus",
            listOf("filter", "hopper", "add", "create", "new")
        ),
        IconDefinition("funnel-x", "Funnel X", listOf("filter", "hopper", "remove", "delete")),
        IconDefinition("gamepad-2", "Gamepad", listOf("console")),
        IconDefinition("ghost", "Ghost", listOf("pac-man", "spooky")),
        IconDefinition("globe", "Globe", listOf("world", "browser", "language", "translate")),
        IconDefinition(
            "globe-lock",
            "Globe Lock",
            listOf(
                "vpn", "private", "privacy", "network", "world", "browser", "security",
                "encryption", "protection", "connection"
            )
        ),
        IconDefinition(
            "globe-x",
            "Globe X",
            listOf(
                "globe", "internet", "offline", "disconnected", "network", "connection", "world",
                "no connection", "network failure", "signal off"
            )
        ),
        IconDefinition(
            "headphone-off",
            "Headphone Off",
            listOf("music", "audio", "sound", "mute", "off")
        ),
        IconDefinition("headphones", "Headphones", listOf("music", "audio", "sound")),
        IconDefinition(
            "history",
            "History",
            listOf(
                "time", "redo", "undo", "rewind", "timeline", "version", "time machine", "backup",
                "rotate", "ccw"
            )
        ),
        IconDefinition("import", "Import", listOf("save")),
        IconDefinition("inbox", "Inbox", listOf("email")),
        IconDefinition("info", "Info", listOf("help")),
        IconDefinition(
            "key-round",
            "Key Round",
            listOf("password", "login", "authentication", "secure", "unlock")
        ),
        IconDefinition("keyboard", "Keyboard", listOf("layout", "spell", "settings", "mouse")),
        IconDefinition(
            "keyboard-off",
            "Keyboard Off",
            listOf("unkeys", "layout", "spell", "settings", "mouse")
        ),
        IconDefinition("languages", "Languages", listOf("translate")),
        IconDefinition("link-2", "Link", listOf("chain", "url")),
        IconDefinition("link-2-off", "Link Off", listOf("unchain", "chain")),
        IconDefinition("lock", "Lock", listOf("security", "password", "secure", "admin")),
        IconDefinition("lock-open", "Lock Open", listOf("security")),
        IconDefinition("log-in", "Log In", listOf("sign in", "arrow", "enter", "auth")),
        IconDefinition("log-out", "Log Out", listOf("sign out", "arrow", "exit", "auth")),
        IconDefinition("magnet", "Magnet", listOf("horseshoe", "lock", "science", "snap")),
        IconDefinition("map-pin", "Map Pin", listOf("location", "waypoint", "marker", "drop")),
        IconDefinition(
            "map-pin-check-inside",
            "Map Pin Check",
            listOf(
                "location", "waypoint", "marker", "drop", "done", "tick", "complete", "task",
                "added"
            )
        ),
        IconDefinition(
            "map-pin-minus-inside",
            "Map Pin Minus",
            listOf("location", "waypoint", "marker", "drop", "delete", "remove", "erase")
        ),
        IconDefinition(
            "map-pin-off",
            "Map Pin Off",
            listOf("location", "waypoint", "marker", "remove")
        ),
        IconDefinition(
            "map-pin-plus-inside",
            "Map Pin Plus",
            listOf("location", "waypoint", "marker", "drop", "add", "create", "new")
        ),
        IconDefinition(
            "map-pin-x-inside",
            "Map Pin X",
            listOf("location", "waypoint", "marker", "drop", "delete", "remove", "erase")
        ),
        IconDefinition("maximize", "Maximize", listOf("fullscreen", "expand", "dashed")),
        IconDefinition(
            "megaphone",
            "Megaphone",
            listOf(
                "advertisement", "announcement", "attention", "alert", "loudspeaker", "megaphone",
                "notification"
            )
        ),
        IconDefinition(
            "megaphone-off",
            "Megaphone Off",
            listOf(
                "advertisement", "announcement", "attention", "alert", "loudspeaker", "megaphone",
                "notification", "disable", "silent"
            )
        ),
        IconDefinition(
            "message-circle",
            "Message Circle",
            listOf("comment", "chat", "conversation", "dialog", "feedback", "speech bubble")
        ),
        IconDefinition(
            "message-circle-off",
            "Message Circle Off",
            listOf(
                "comment", "chat", "conversation", "dialog", "feedback", "speech bubble", "clear",
                "close", "delete", "remove", "cancel", "silence", "mute", "moderate"
            )
        ),
        IconDefinition(
            "message-circle-question-mark",
            "Message Circle Question Mark",
            listOf("comment", "chat", "conversation", "dialog", "feedback", "speech bubble", "help")
        ),
        IconDefinition(
            "message-circle-warning",
            "Message Circle Warning",
            listOf(
                "comment", "chat", "conversation", "dialog", "feedback", "speech bubble", "report",
                "abuse", "offense", "alert", "danger", "caution", "protected", "exclamation mark"
            )
        ),
        IconDefinition(
            "mic",
            "Mic",
            listOf("record", "sound", "listen", "radio", "podcast", "microphone")
        ),
        IconDefinition("mic-off", "Mic Off", listOf("record", "sound", "mute", "microphone")),
        IconDefinition("minimize", "Minimize", listOf("exit fullscreen", "close", "shrink")),
        IconDefinition(
            "minus",
            "Minus",
            listOf(
                "subtract", "remove", "decrease", "decrement", "reduce", "negative", "calculate",
                "line", "divider", "separator", "horizontal rule", "hr", "html", "markup",
                "markdown", "---", "toolbar", "operator", "code", "coding", "minimum", "downgrade"
            )
        ),
        IconDefinition(
            "monitor",
            "Monitor",
            listOf("tv", "screen", "display", "virtual machine", "vm")
        ),
        IconDefinition("monitor-off", "Monitor Off", listOf("share")),
        IconDefinition("moon", "Moon", listOf("dark", "night")),
        IconDefinition("mouse", "Mouse", listOf("device", "scroll", "click")),
        IconDefinition("mouse-off", "Mouse Off", listOf("device", "scroll", "click", "disabled")),
        IconDefinition("mouse-pointer-2", "Mouse Pointer", listOf("click", "select")),
        IconDefinition(
            "mouse-pointer-2-off",
            "Mouse Pointer Off",
            listOf(
                "pointer", "mouse", "cursor", "off", "disable", "arrow", "navigation", "selection",
                "select", "click", "no-click", "interaction"
            )
        ),
        IconDefinition("mouse-pointer-click", "Mouse Pointer Click", listOf("click", "select")),
        IconDefinition("nfc", "NFC", listOf("contactless", "payment", "near-field communication")),
        IconDefinition("pause", "Pause", listOf("music", "stop")),
        IconDefinition(
            "pencil",
            "Pencil",
            listOf(
                "rubber", "edit", "create", "draw", "sketch", "draft", "writer", "writing",
                "stationery", "artist"
            )
        ),
        IconDefinition(
            "pencil-off",
            "Pencil Off",
            listOf(
                "disabled", "inactive", "non-editable", "locked", "read-only", "unmodifiable",
                "frozen", "restricted", "rubber", "edit", "create", "draw", "sketch", "draft",
                "writer", "writing", "stationery", "artist"
            )
        ),
        IconDefinition("phone", "Phone", listOf("call")),
        IconDefinition("phone-incoming", "Phone Incoming", listOf("call")),
        IconDefinition("phone-off", "Phone Off", listOf("call", "mute")),
        IconDefinition("phone-outgoing", "Phone Outgoing", listOf("call")),
        IconDefinition("pin", "Pin", listOf("save", "map", "lock", "fix")),
        IconDefinition(
            "pin-off",
            "Pin Off",
            listOf("unpin", "map", "unlock", "unfix", "unsave", "remove")
        ),
        IconDefinition("plane", "Plane", listOf("plane", "trip", "airplane")),
        IconDefinition("play", "Play", listOf("music", "audio", "video", "start", "run")),
        IconDefinition(
            "plug",
            "Plug",
            listOf(
                "electricity", "energy", "electronics", "socket", "outlet", "power", "voltage",
                "current", "charger"
            )
        ),
        IconDefinition(
            "plug-zap",
            "Plug Zap",
            listOf(
                "electricity", "energy", "electronics", "charge", "charging", "battery", "connect"
            )
        ),
        IconDefinition(
            "plus",
            "Plus",
            listOf(
                "add", "new", "increase", "increment", "positive", "calculate", "toolbar",
                "crosshair", "aim", "target", "scope", "sight", "reticule", "maximum", "upgrade",
                "extra", "+"
            )
        ),
        IconDefinition(
            "power",
            "Power",
            listOf(
                "on", "off", "device", "switch", "toggle", "binary", "boolean", "reboot",
                "restart", "button", "keyboard", "troubleshoot"
            )
        ),
        IconDefinition("power-off", "Power Off", listOf("on", "off", "device", "switch")),
        IconDefinition(
            "qr-code",
            "QR Code",
            listOf("barcode", "scan", "link", "url", "information", "digital")
        ),
        IconDefinition(
            "receipt",
            "Receipt",
            listOf(
                "bill", "voucher", "slip", "check", "counterfoil", "currency", "dollar", "usd", "$"
            )
        ),
        IconDefinition(
            "receipt-text",
            "Receipt Text",
            listOf(
                "bill", "voucher", "slip", "check", "counterfoil", "details", "small print",
                "terms", "conditions", "contract"
            )
        ),
        IconDefinition("redo-2", "Redo", listOf("undo", "rerun", "history")),
        IconDefinition(
            "refresh-cw",
            "Refresh CW",
            listOf(
                "rotate", "reload", "rerun", "synchronise", "synchronize", "arrows", "circular",
                "cycle"
            )
        ),
        IconDefinition(
            "refresh-cw-off",
            "Refresh CW Off",
            listOf(
                "rotate", "reload", "rerun", "synchronise", "synchronize", "arrows", "circular",
                "cycle", "cancel", "no", "stop", "error", "disconnect", "ignore"
            )
        ),
        IconDefinition("repeat", "Repeat", listOf("loop", "arrows")),
        IconDefinition("rewind", "Rewind", listOf("music")),
        IconDefinition(
            "rotate-ccw",
            "Rotate CCW",
            listOf(
                "arrow", "left", "counter-clockwise", "restart", "reload", "rerun", "refresh",
                "backup", "undo", "replay", "redo", "retry", "rewind", "reverse"
            )
        ),
        IconDefinition(
            "rotate-cw",
            "Rotate CW",
            listOf("arrow", "right", "clockwise", "refresh", "reload", "rerun", "redo")
        ),
        IconDefinition("save", "Save", listOf("floppy disk")),
        IconDefinition("save-all", "Save All", listOf("floppy disks", "copy")),
        IconDefinition("save-off", "Save Off", listOf("floppy disk", "unsalvageable")),
        IconDefinition(
            "scissors",
            "Scissors",
            listOf("cut", "snip", "chop", "stationery", "crafts")
        ),
        IconDefinition("screen-share", "Screen Share", listOf("host", "desktop", "monitor")),
        IconDefinition(
            "screen-share-off",
            "Screen Share Off",
            listOf("desktop", "disconnect", "monitor")
        ),
        IconDefinition(
            "search",
            "Search",
            listOf("find", "scan", "magnifier", "magnifying glass", "lens")
        ),
        IconDefinition(
            "send",
            "Send",
            listOf("email", "message", "mail", "paper airplane", "paper aeroplane", "submit")
        ),
        IconDefinition("settings", "Settings", listOf("cog", "edit", "gear", "preferences")),
        IconDefinition(
            "settings-2",
            "Settings Alt",
            listOf("cog", "edit", "gear", "preferences", "slider")
        ),
        IconDefinition("share", "Share", listOf("network", "connections")),
        IconDefinition("share-2", "Share", listOf("network", "connections")),
        IconDefinition(
            "shield",
            "Shield",
            listOf(
                "cybersecurity", "secure", "safety", "protection", "guardian", "armored",
                "armoured", "defense", "defence", "defender", "block", "threat", "prevention",
                "antivirus", "vigilance", "vigilant", "detection", "scan", "find", "strength",
                "strong", "tough", "invincible", "invincibility", "invulnerable", "undamaged",
                "audit", "admin", "verification", "crest", "bravery", "knight", "foot soldier",
                "infantry", "trooper", "pawn", "battle", "war", "military", "army", "cadet", "scout"
            )
        ),
        IconDefinition(
            "shield-off",
            "Shield Off",
            listOf(
                "unshielded", "cybersecurity", "insecure", "unsecured", "safety", "unsafe",
                "protection", "unprotected", "guardian", "unguarded", "unarmored", "unarmoured",
                "defenseless", "defenceless", "undefended", "defender", "interception", "threat",
                "prevention", "unprevented", "antivirus", "detection", "undetected", "exploit",
                "vulnerability", "vulnerable", "weakness", "infected", "infection", "comprimised",
                "data leak", "unaudited", "admin", "verification", "unverified", "inactive",
                "cancelled", "error", "crest", "bravery", "damaged", "injured", "hit", "expired",
                "eliminated"
            )
        ),
        IconDefinition(
            "shopping-cart",
            "Shopping Cart",
            listOf(
                "trolley", "cart", "basket", "e-commerce", "store", "purchase", "products",
                "items", "ingredients"
            )
        ),
        IconDefinition("shrink", "Shrink", listOf("scale", "fullscreen")),
        IconDefinition("shuffle", "Shuffle", listOf("music", "random", "reorder")),
        IconDefinition(
            "signal",
            "Signal",
            listOf("connection", "wireless", "gsm", "phone", "2g", "3g", "4g", "5g")
        ),
        IconDefinition(
            "signal-high",
            "Signal High",
            listOf("connection", "wireless", "gsm", "phone", "2g", "3g", "4g", "5g")
        ),
        IconDefinition(
            "signal-low",
            "Signal Low",
            listOf("connection", "wireless", "gsm", "phone", "2g", "3g", "4g", "5g")
        ),
        IconDefinition(
            "signal-medium",
            "Signal Medium",
            listOf("connection", "wireless", "gsm", "phone", "2g", "3g", "4g", "5g")
        ),
        IconDefinition("skip-back", "Skip Back", listOf("arrow", "previous", "music")),
        IconDefinition("skip-forward", "Skip Forward", listOf("arrow", "skip", "next", "music")),
        IconDefinition(
            "square",
            "Square",
            listOf(
                "stop", "playback", "music", "audio", "video", "rectangle", "aspect ratio", "1:1",
                "shape"
            )
        ),
        IconDefinition("star", "Star", listOf("bookmark", "favorite", "like", "review", "rating")),
        IconDefinition("star-off", "Star Off", listOf("dislike", "unlike", "remove", "unrate")),
        IconDefinition("sun", "Sun", listOf("brightness", "weather", "light", "summer")),
        IconDefinition(
            "sun-moon",
            "Sun Moon",
            listOf(
                "dark", "light", "moon", "sun", "brightness", "theme", "auto theme",
                "system theme", "appearance"
            )
        ),
        IconDefinition("switch-camera", "Switch Camera", listOf("photo", "selfie", "front", "back")),
        IconDefinition("tag", "Tag", listOf("label", "badge", "ticket", "mark")),
        IconDefinition(
            "tags",
            "Tags",
            listOf("labels", "badges", "tickets", "marks", "copy", "multiple")
        ),
        IconDefinition("terminal", "Terminal", listOf("code", "command line", "prompt", "shell")),
        IconDefinition("text-cursor-input", "Text Cursor", listOf("select")),
        IconDefinition("timer", "Timer", listOf("time", "timer", "stopwatch")),
        IconDefinition("timer-off", "Timer Off", listOf("time", "timer", "stopwatch")),
        IconDefinition("timer-reset", "Timer Reset", listOf("time", "timer", "stopwatch")),
        IconDefinition(
            "trash",
            "Trash",
            listOf(
                "empty", "deletion", "cleanup", "junk", "clear", "garbage", "delete", "remove",
                "bin", "waste", "recycle", "discard", "binoculars", "rubbish"
            )
        ),
        IconDefinition(
            "triangle-alert",
            "Triangle Alert",
            listOf("warning", "alert", "danger", "exclamation mark", "linter")
        ),
        IconDefinition("type", "Type", listOf("text", "font", "typography")),
        IconDefinition(
            "undo-2",
            "Undo",
            listOf(
                "redo", "rerun", "history", "back", "return", "reverse", "revert", "direction",
                "u-turn"
            )
        ),
        IconDefinition(
            "unplug",
            "Unplug",
            listOf("electricity", "energy", "electronics", "socket", "outlet", "disconnect")
        ),
        IconDefinition("upload", "Upload", listOf("file")),
        IconDefinition(
            "usb",
            "USB",
            listOf("universal", "serial", "bus", "controller", "connector", "interface")
        ),
        IconDefinition("user", "User", listOf("person", "account", "contact")),
        IconDefinition("user-cog", "User Cog", listOf("settings", "edit", "cog", "gear")),
        IconDefinition("users", "Users", listOf("group", "people")),
        IconDefinition(
            "vibrate",
            "Vibrate",
            listOf("smartphone", "notification", "rumble", "haptic feedback", "screen")
        ),
        IconDefinition(
            "vibrate-off",
            "Vibrate Off",
            listOf(
                "smartphone", "notification", "rumble", "haptic feedback", "notifications", "screen"
            )
        ),
        IconDefinition(
            "video",
            "Video",
            listOf("camera", "movie", "film", "recording", "motion picture", "camcorder", "reel")
        ),
        IconDefinition("video-off", "Video Off", listOf("camera", "movie", "film")),
        IconDefinition("volume", "Volume", listOf("music", "sound", "mute", "speaker")),
        IconDefinition("volume-1", "Volume Low", listOf("music", "sound", "speaker")),
        IconDefinition("volume-2", "Volume", listOf("music", "sound", "speaker")),
        IconDefinition("volume-off", "Volume Off", listOf("music", "sound", "mute", "speaker")),
        IconDefinition("volume-x", "Volume X", listOf("music", "sound", "mute", "speaker")),
        IconDefinition("wifi", "Wi-Fi", listOf("connection", "signal", "wireless")),
        IconDefinition(
            "wifi-cog",
            "Wi-Fi Settings",
            listOf(
                "connection", "signal", "wireless", "directory", "settings", "control",
                "preferences", "cog", "edit", "gear"
            )
        ),
        IconDefinition("wifi-high", "Wi-Fi High", listOf("connection", "signal", "wireless")),
        IconDefinition("wifi-low", "Wi-Fi Low", listOf("connection", "signal", "wireless")),
        IconDefinition("wifi-off", "Wi-Fi Off", listOf("disabled")),
        IconDefinition(
            "wifi-sync",
            "Wi-Fi Sync",
            listOf(
                "connection", "signal", "wireless", "synchronize", "reconnect", "reset", "restart"
            )
        ),
        IconDefinition(
            "wrench",
            "Wrench",
            listOf("account", "settings", "spanner", "diy", "toolbox", "build", "construction")
        ),
        IconDefinition(
            "x",
            "X",
            listOf(
                "cancel", "close", "cross", "delete", "ex", "remove", "times", "clear", "math",
                "multiply", "multiplication"
            )
        ),
        IconDefinition(
            "zap",
            "Zap",
            listOf("flash", "camera", "lightning", "electricity", "energy")
        ),
        IconDefinition(
            "zap-off",
            "Zap Off",
            listOf("flash", "camera", "lightning", "electricity", "energy")
        ),
        IconDefinition("zoom-in", "Zoom In", listOf("magnifying glass", "plus")),
        IconDefinition("zoom-out", "Zoom Out", listOf("magnifying glass", "plus"))

    )

    private val icons: List<CommandIcon> by lazy {
        iconDefinitions().map { def ->
            val icon = IconLoader.getIcon(
                "/icons/commands/${def.id}.svg",
                CommandIconsProvider::class.java
            )
            CommandIcon(def.id, def.displayName, def.tags, icon)
        }.sortedBy { it.displayName.lowercase() }
    }

    private val iconsByIdMap: Map<String, CommandIcon> by lazy {
        icons.associateBy { it.id }
    }

    fun getIconById(id: String): CommandIcon? = iconsByIdMap[id]

    fun getIcon(id: String, color: IconColor): Icon? {
        val commandIcon = getIconById(id) ?: return null
        return ColoredIcon(commandIcon.icon, color.color)
    }

    fun search(query: String): List<CommandIcon> {
        if (query.isBlank()) return icons
        val lowerQuery = query.lowercase()
        return icons.filter { icon ->
            icon.displayName.lowercase().contains(lowerQuery) ||
                icon.id.lowercase().contains(lowerQuery) ||
                icon.tags.any { tag -> tag.contains(lowerQuery) }
        }
    }
}
