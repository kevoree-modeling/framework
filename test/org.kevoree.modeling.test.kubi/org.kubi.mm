with version "1-SNAPSHOT"
with kmfVersion "4.19.1-SNAPSHOT"

class org.kubi.Ecosystem {
    att name: String
    ref* groupes: org.kubi.Group
    ref* technologies: org.kubi.Technology
}

class org.kubi.Group {
    att name: String
    ref* groupes: org.kubi.Group
    ref* devices: org.kubi.Device with opposite "groupes"
}

class org.kubi.Device {
    att id: String
    att homeId: String
    ref* groupes: org.kubi.Group with opposite "devices"
    ref* links: org.kubi.Device
    att name: String
    ref technology: org.kubi.Technology
    ref* stateParameters: org.kubi.StateParameter
    ref* actionParameters: org.kubi.ActionParameter
    ref productType: org.kubi.Product
}

enum org.kubi.ParameterType {
    list, byte, short, bool, int, button, decimal, string
}

class org.kubi.StateParameter {
    att name: String
    att value: String
    att valueType: String
    att precision: Double
    att unit: String
    att range: String
}

class org.kubi.ActionParameter extends org.kubi.StateParameter {
    att desired: String
}

class org.kubi.Technology {
    att name: String
    ref* devices: org.kubi.Device with opposite "technology"
    ref catalog: org.kubi.Catalog
}

class org.kubi.Catalog {
    ref* manufacturers: org.kubi.Manufacturer
}

class org.kubi.Manufacturer {
    att name: String
    att id: Int
    ref* products: org.kubi.Product
}

class org.kubi.Product {
    att id: Int
    att name: String
    att version: String
    att pictureUrl: String
    ref manufacturer: org.kubi.Manufacturer with opposite "products"
    ref* devices: org.kubi.Device with opposite "productType"
}

class org.kubi.ZWaveProduct extends org.kubi.Product {
    ref* commandClasses: org.kubi.zwave.CommandClass
    att type: Int
    att configUrl: String
    att loaded: Bool
}

class org.kubi.zwave.CommandClass {
    att id: Int
    ref* parameters: org.kubi.zwave.Parameter
    ref* associations: org.kubi.zwave.Association
}

class org.kubi.zwave.Parameter {
    att type: org.kubi.ParameterType
    att name: String
    att help: String
    att genre: String
    att instance: Int
    att index: Int
    att label: String
    att value: String
    att min: Long
    att min: Long
    att max: Long
    att size: Int
    ref* items: org.kubi.zwave.ParameterItem
}

class org.kubi.zwave.Association {
    att numGroups: Int
    ref* groups: org.kubi.zwave.AssociationGroup
}

class org.kubi.zwave.AssociationGroup {
    att index: Int
    att maxAssociations: Int
    att label: String
    att auto: Bool
}

class org.kubi.zwave.ParameterItem {
    att label: String
    att value: Int
}