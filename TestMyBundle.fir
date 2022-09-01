circuit TestMyBundle :
  module TestMyBundle :
    input clock : Clock
    input reset : UInt<1>

    wire bundle : { foo : UInt<4>, bar : UInt<3>} @[Bundle.scala 20:20]
    bundle.foo <= UInt<4>("hc") @[Bundle.scala 21:14]
    bundle.bar <= UInt<2>("h3") @[Bundle.scala 22:14]
    node uint = cat(bundle.foo, bundle.bar) @[Bundle.scala 23:21]

