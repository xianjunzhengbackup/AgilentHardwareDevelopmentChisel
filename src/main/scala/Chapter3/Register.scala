package Chapter3
import chisel3._
import chisel3.stage.ChiselStage
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WrapRegBundle extends Module{
  val io = IO(new Bundle() {
    val in = Input(Vec(4,UInt(8.W)))
    val out = Output(UInt(4.W))
  })
//  val r0 = Reg(UInt())  //这样写会报错，因为没指定寄存器的宽度，而且r0没有被引用到，无法infer它的宽度
  val r1 = Reg(UInt(8.W))
//  val r2 = Reg(Vec(4, UInt()))
  val r3 = Reg(Vec(4, UInt(8.W)))
  val r4 = VecInit(15.U(4.W),15.U(4.W),9.U(4.W),1.U(4.W))
  val r5 = io.in.map(x=>x.andR) //将io的每一项都按位and一遍，得到4个布尔值的Seq
  val r6 = r5.map(x=>{
    val res = Mux(x,1.U(4.W),0.U(4.W))
    res
  }) zip Seq(8.U(4.W),4.U(4.W),2.U(4.W),1.U(4.W))
  io.out := (r6 map(x=>{
    x._1 * x._2
  })) reduce (_+_)  //将上面得到的4个布尔值映射成UInt，输出到out。注意io.in(0)是最高位
}

class BasicTestRegBundle extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "WrapRegBundle"
  // test class body here
  it should "check x4 ways to implement Reg" in {
    // test case body here
    test(new WrapRegBundle) {c=>
      c.io.in(0).poke(255)
      c.io.in(1).poke(2)
      c.io.in(2).poke(255)
      c.io.in(3).poke(255)
      c.clock.step()
      println(c.io.out.peek().litValue)
    }
  }
  println((new ChiselStage).emitVerilog(new WrapRegBundle))
}
