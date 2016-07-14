import static lib.BDD.*;

SQLCONFIG(
        db: 'fee',
        user: 'fee'
)

SQL("delete from orders")
SQL("delete from products")

CONFIG(
        server: "http://127.0.0.1",
        headers: [host: "test.fee.pay.sogou.com"]
)

GET("/api/phoneinfo") {
    r.query = [phones: "18600539639"]
}
EXPECT {
    json.code = 0
    json.'data' = NotEmpty
    json.'data' = Grep {
        obj.operator = "CHINA_UNICOM"
        obj.provinceId = "110000"
    }
}

GET("/api/phoneinfo") {
    r.query = [phones: "15811330571"]
}
EXPECT {
    json.code = 0
    json.'data' = NotEmpty
    json.'data' = Grep {
        obj.operator = "CHINA_MOBILE"
        obj.provinceId = "110000"
    }
}

GET("/api/phoneinfo") {
    r.query = [phones: "18875617560"]
}
EXPECT {
    json.code = 0
    json.'data' = NotEmpty
    json.'data' = Grep {
        obj.operator = "CHINA_MOBILE"
        obj.provinceId = "130000"
    }
}

GET("/api/phoneinfo")
EXPECT {
    json.code = 400
}

GET("/api/phoneinfo") {
    r.query = [phones: "111"]
}
EXPECT {
    json.code = 400
}


GET("/api/phoneProducts") {
    r.query = [feeType : "PHONE",
               operator: "CHINA_MOBILE",
               province: "110000"]
}
def phoneProNum
def phoneProductId
EXPECT {
    json.code = 0
    json.'data.length()' > 0
    json.closure = { json ->
        phoneProNum = json.'data.length()'
        phoneProductId = json.'data[0].productId'
    }
}

def productCount
SQL("select count(*)  as num from products") { row ->
    productCount = row.num
    assert productCount == phoneProNum
}

GET("/api/phoneProducts") {
    r.query = [feeType : "FLOW",
               operator: "CHINA_MOBILE",
               province: "110000"]
}
def flowProNum
def flowProductId
EXPECT {
    json.code = 0
    json.'data.length()' > 0
    json.closure = { json ->
        flowProNum = json.'data.length()'
        flowProductId = json.'data[0].productId'
    }
}

SQL("select count(*)  as num from products") { row ->
    productCount = row.num
    assert productCount == (phoneProNum + flowProNum)
}

println "phoneProNum:$phoneProNum"
println "flowProNum:$flowProNum"
println "productCount:$productCount"
println "phoneProductId:$phoneProductId"
println "flowProductId:$flowProductId"



GET("/api/phoneProducts") {
    r.query = [feeType : "PHONE",
               operator: "CHINA_MOBILE",
               province: "110000"]
}
GET("/api/phoneProducts") {
    r.query = [feeType : "FLOW",
               operator: "CHINA_MOBILE",
               province: "110000"]
}


SQL("select count(*)  as num from products") { row ->
    productCountNew = row.num
    println "productCountNew:$productCountNew"
    assert productCountNew == productCount
}

GET("/api/phoneProducts") {
    r.query = [feeType : "FLOW",
               province: "110000"]
}
EXPECT {
    json.code = 400
}
GET("/api/phoneProducts") {
    r.query = [feeType : "FLOW",
               operator: "CHINA_MOBILE"]
}
EXPECT {
    json.code = 400
}

POST("/api/order") {
    r.body = [feeType    : "PHONE",
              productId  : phoneProductId,
              quantity   : 1,
              payChannel : "ZHIFUBAO",
              payTerminal: "WAP",
              province   : "110000",
              operator   : "CHINA_MOBILE"
    ]
}
EXPECT {
    json.code = 400
}
POST("/api/order") {
    r.body = [feeType    : "PHONE",
              phone      : "15811330571",
              quantity   : 1,
              payChannel : "ZHIFUBAO",
              payTerminal: "WAP",
              province   : "110000",
              operator   : "CHINA_MOBILE"
    ]
}
EXPECT {
    json.code = 400
}

POST("/api/order") {
    r.body = [feeType    : "PHONE",
              productId  : phoneProductId,
              phone      : "15811330571",
              quantity   : 1,
              payChannel : "ZHIFUBAO",
              payTerminal: "WAP",
              province   : "110000",
              operator   : "CHINA_UNICOM"
    ]
}
EXPECT {
    json.code = 500
}

POST("/api/order") {
    r.body = [feeType    : "PHONE",
              productId  : phoneProductId,
              phone      : "15811330571",
              quantity   : 1,
              payChannel : "ZHIFUBAO",
              payTerminal: "WAP",
              province   : "130000",
              operator   : "CHINA_MOBILE"
    ]
}
EXPECT {
    json.code = 500
}

POST("/api/order") {
    r.body = [feeType    : "PHONE",
              productId  : phoneProductId,
              phone      : "15811330571",
              quantity   : 1,
              payTerminal: "WAP",
              province   : "110000",
              operator   : "CHINA_MOBILE"
    ]
}
EXPECT {
    json.code = 400
}

POST("/api/order") {
    r.body = [feeType   : "PHONE",
              productId : phoneProductId,
              phone     : "15811330571",
              quantity  : 1,
              payChannel: "ZHIFUBAO",
              province  : "110000",
              operator  : "CHINA_MOBILE"
    ]
}
EXPECT {
    json.code = 400
}

POST("/api/order") {
    r.body = [feeType    : "PHONE",
              productId  : phoneProductId,
              quantity   : 2,
              phone      : "15811330571",
              payChannel : "ZHIFUBAO",
              payTerminal: "WAP",
              province   : "110000",
              operator   : "CHINA_MOBILE"
    ]
}
EXPECT {
    json.code = 400
}

def orderCountOld
SQL("select count(*) as num from orders") { row ->
    orderCountOld = row.num
}


POST("/api/order") {
    r.body = [feeType    : "PHONE",
              productId  : phoneProductId,
              phone      : "15811330571",
              quantity   : 1,
              payChannel : "ZHIFUBAO",
              payTerminal: "WAP",
              province   : "110000",
              operator   : "CHINA_MOBILE"
    ]
}
def flowOrderId
EXPECT {
    json.code = 0
    json.closure = { json ->
        flowOrderId = json.'data.order.orderId'
        status = json.'data.order.status'
        assert status.equals("TOPAY")
        payInfo = json.'data.paymentInfo'
        payReturnType = json.'data.payReturnType'
        println "payinfo:$payInfo"
        println "payReturnType:$payReturnType"
    }
}

def orderCount
SQL("select count(*) as num from orders") { row ->
    orderCount = row.num
    assert orderCount == (orderCountOld + 1)
}
println "orderCount:$orderCountOld"
println "orderCount:$orderCount"


STAT()