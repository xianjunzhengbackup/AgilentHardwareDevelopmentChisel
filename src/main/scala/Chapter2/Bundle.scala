package Chapter2
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

/*
自定义一个bundle类，然后通过asUint method将里面的字段合并
 */
class MyBundle extends Bundle{
  val foo = UInt(4.W) //高位
  val bar = UInt(3.W) //低位
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

class BundleWithVec extends Bundle {
  val foo = UInt(4.W) //高位
  val bar = Vec(2,UInt(5.W))  //低位
}
class WrapBundleWithVec extends Module{
  val bundle = Wire(new BundleWithVec)
  bundle.foo := 0x0.U
  bundle.bar(0) := 0x0.U
  bundle.bar(1) := 0x1.U
  val uint = bundle.asUInt
}
/*
新建一个object用于执行测试类
代码中的println不会打印出0xc3，只会打印出 TestMyBundle.uint: OpResult[UInt<7>]
针对每一个待测的类，都要执行一句 (new ChiselStage).execute(Array(),Seq(ChiselGeneratorAnnotation(()=> {....}
 */
object Bundle extends App{
  (new ChiselStage).execute(Array(),Seq(ChiselGeneratorAnnotation(()=> {
    val BundleObject = new TestMyBundle
    println(BundleObject.uint)
    BundleObject})))

  (new ChiselStage).execute(Array(),Seq(ChiselGeneratorAnnotation(()=> {
    val BundleWithVecObj = new WrapBundleWithVec
    println(BundleWithVecObj.uint)
    BundleWithVecObj})))
}

/*
要实现Verilog的如下功能：
wire [1:0] a;
wire [3:0] b;
wire [2:0] c;

wire [8:0] z = 0x1ff
assign {a,b,c} = z;

chisel中不能直接这样赋值，最简单的做法是将a，b，c组成一个bundle，高位在前。然后创建一个线网z，z可以被直接赋值。z调用asTypeOf method
该method接收一个Data类型的参数。这里就是传入由a，b，c组成的bundle作为参数。返回的对象就是按照z来赋值的
 */
class CatBundle extends Bundle{
  val a = UInt(2.W)
  val b = UInt(4.W)
  val c = UInt(3.W)
}
object CatBundleObj extends App{
  (new ChiselStage).execute(Array(),Seq(ChiselGeneratorAnnotation(()=> {
    val c = new Module{}
    val z = Wire(UInt(9.W))
    z := 0xff.U
    val unpacked = z.asTypeOf(new CatBundle)
    //返回的unpacked.a unpacked.b unpacked.c就是对应于a b c
    println(unpacked.a)
    println(unpacked.b)
    println(unpacked.c)
    c
  })))
}

//