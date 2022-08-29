package Chapter2
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
/*
自定义一个bundle类，然后通过asUint method将里面的字段合并
 */
class MyBundle extends Bundle{
  val foo = UInt(4.W)
  val bar = UInt(3.W)
}

/*
为了测试这个自定义的bundle类，新建一个TestMyBundle类，该类必须继承自Module，否则chisel会报错
在该测试类中进行想要的测试
 */
class TestMyBundle extends Module{
  val bundle = Wire(new MyBundle)
  bundle.foo := 0xc.U
  bundle.bar := 0x3.U
  val uint = bundle.asUInt
}

/*
新建一个object用于执行测试类
 */
object Bundle extends App{
  (new ChiselStage).execute(Array(),Seq(ChiselGeneratorAnnotation(()=> {
    val BundleObject = new TestMyBundle
    println(BundleObject.uint)
    BundleObject})))
}



