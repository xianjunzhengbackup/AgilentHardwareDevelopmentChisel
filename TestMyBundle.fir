circuit TestMyBundle :
  module TestMyBundle :
    input clock : Clock
    input reset : UInt<1>

    wire bundle : { foo : UInt<4>, bar : UInt<3>} @[Bundle.scala 11:20]
    bundle.foo <= UInt<4>("hc") @[Bundle.scala 12:14]
    bundle.bar <= UInt<2>("h3") @[Bundle.scala 13:14]
    node uint = cat(bundle.foo, bundle.bar) @[Bundle.scala 14:21]

