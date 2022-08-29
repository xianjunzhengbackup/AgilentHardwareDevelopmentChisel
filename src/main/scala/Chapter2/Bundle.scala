package Chapter2
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

class MyBundle extends Bundle{
  val foo = UInt(4.W)
  val bar = UInt(3.W)
}

class TestMyBundle extends Module{
  val bundle = Wire(new MyBundle)
  bundle.foo := 0xc.U
  bundle.bar := 0x3.U
  val uint = bundle.asUInt
}
object Bundle extends App{
  (new ChiselStage).execute(Array(),Seq(ChiselGeneratorAnnotation(()=> {
    val BundleObject = new TestMyBundle
    println(BundleObject.uint)
    BundleObject})))
}



