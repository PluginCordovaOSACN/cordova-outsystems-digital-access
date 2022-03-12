class Result {
    var timeout: Int64
    var numberOfBadge: String
    var method: String
    var success: Bool
    var message: String
    var badgeCode: String
    var deviceMac: String
    var deviceName: String
    var dbDistance: Int
    var date: Date
    var location: String
    var isTimeout: Bool
    var sdkVersion: Int //only for android


    init(method: String){
        self.method = method
    }
}
