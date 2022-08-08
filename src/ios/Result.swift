class Result: Encodable {
    var timeout: Int64
    var numberOfBadge: String?
    var method: String
    var success: Bool
    var message: String?
    var badgeCode: String?
    var deviceMac: String?
    var deviceName: String?
    var deviceId: String?
    var dbDistance: Int
    var date: Date
    var location: String?
    var isTimeout: Bool?
    var sdkVersion: Int? //only for android
    var otherMessage: String?
    var userDevice: String?
    var deviceEnabled: Bool?
    var deviceIconsVisible: Bool?
    var deviceIconReversed: Bool?
    var deviceCloseEnough: Bool?
    var deviceIsAlive: Bool?
    var maxMeterDistance: Double?
    var isCloseEnoughBK: Bool?
    var minDistance: Int?
    var minDistanceBK: Int?
    var distanceMeter: Double?
    init(method: String) {
        timeout = 60
        self.method = method
        self.success = false
        message = ""
        otherMessage = ""
        dbDistance = 40
        date = Date()
        self.method = method
        self.userDevice = "iOS"
    }
}
