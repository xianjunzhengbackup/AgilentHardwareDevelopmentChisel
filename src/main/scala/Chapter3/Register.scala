package Chapter3
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WrapRegBundle extends Module{
  val io = IO(new Bundle() {
    val in = Input(Vec(4,UInt(8.W)))
    val out = Output(UInt(4.W))
  })
//  val r0 = Reg(UInt())
  val r1 = Reg(UInt(8.W))
//  val r2 = Reg(Vec(4, UInt()))
  val r3 = Reg(Vec(4, UInt(8.W)))
  val r4 = VecInit(15.U(4.W),15.U(4.W),9.U(4.W),1.U(4.W))
//  val r5 = io.in.map(x=>x.andR).map(x=>{
//    if(x==true) 1
//    else 0
//  }) zip Seq(8,4,2,1)
  val r5 = io.in.map(x=>x.andR)
  println(r5)
  val r6 = r5.map{
    case true.B => 1.U(4.W)
    case false.B => 0.U(4.W)
  } zip Seq(8.U(4.W),4.U(4.W),2.U(4.W),1.U(4.W))
  println(r6)
  io.out := r6.map{
    case(x_1:UInt,x_2:UInt)=> x_1 * x_2
  }.reduce(_ + _)
//  io.out := (r6.map(x => x._1).sum)
}

class BasicTestRegBundle extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "WrapRegBundle"
  // test class body here
  it should "check x4 ways to implement Reg" in {
    // test case body here
   println((new ChiselStage).emitVerilog(new WrapRegBundle))
  }
}

object test extends App{
  val a = Seq(true,false,true,true)
  val b=a.map(x=>{
    if(x==true) 1
    else 0
  })
  val c = b zip Seq(8,4,2,1)
  println(c.map(x => x._1 * x._2 ))
}
