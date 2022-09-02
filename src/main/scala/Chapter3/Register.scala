package Chapter3
import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util.{RegEnable, ShiftRegister}
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
//  println((new ChiselStage).emitVerilog(new WrapRegBundle))
}

class WrapRegNext extends Module{
  val io = IO(new Bundle() {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })

  /*
  RegNext返回一个位宽可以自动推断的寄存器。有两个版本，一个不带初始化值，一个带初始化值
  用于构建shift register
  def apply[T <: Data](next: T, init: T)(implicit sourceInfo: SourceInfo, compileOptions: CompileOptions): T
  def apply[T <: Data](next: T)(implicit sourceInfo: SourceInfo, compileOptions: CompileOptions): T
  还是寄存器，只不过指定了寄存器的输入来自哪里
   */
  val regA = RegNext(io.in)
  val regB = RegNext(regA,0.U)
  val regC = RegNext(regB,0.U)
  io.out := regC
}
class BasicTestRegNext extends AnyFlatSpec with ChiselScalatestTester{
  behavior of "WrapRegNext"
  // test class body here
  it should "check behavior" in {
    test(new WrapRegNext){c=>
      (1 to 10) foreach(n=>{
        c.io.in.poke(n)
        c.clock.step()
        println(s"$n : output is " + c.io.out.peek().litValue)
      })
    }
  }
}

/*
RegInit 复位时可以到指定值的寄存器
有两种apply method
第一种 单参数模式 def apply[T <: Data](init: T)(implicit sourceInfo: SourceInfo, compileOptions: CompileOptions): T
第二种 双参数模式 def apply[T <: Data](t: T, init: T)(implicit sourceInfo: SourceInfo, compileOptions: CompileOptions): T
还是寄存器，只是增加了复位时的初始化值
 */
class WrapRegInit extends Module{
  val io = IO(new Bundle() {
    val in = Input(UInt(8.W))
    val out = Output(UInt(8.W))
  })
  val r1 = RegInit(1.U) //单参数模式 位宽自动推断为1
  val r2 = RegInit(1.U(8.W))  //位宽为8

  val r3 = RegInit(UInt(8.W),9.U) //双参数模式 第一个参数表示数据类型，第二个是复位时的value
  r3 := io.in
  io.out := r3

}

class BasicTestRegInit extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "RegInit"
  // test class body here
  it should "将检查RegInit的输出" in {
    test(new WrapRegInit) {c=>
      (1 to 10) foreach(n=>{
        c.io.in.poke(n)
        c.clock.step()
        println(s"$n:output is "+c.io.out.peek().litValue)
      })
      c.reset.poke(true.B)
      c.clock.step()
      println(s"After reset:output is "+c.io.out.peek().litValue)
    }
  }
  println((new ChiselStage).emitVerilog(new WrapRegInit))
}

/*
带使能的寄存器
也有两种apply method。与RegNext一样，两种模式都指定了数据的来源
第一种def apply[T <: Data](next: T, init: T, enable: Bool): T ，带初始化值
第二种def apply[T <: Data](next: T, enable: Bool): T ，不带初始化值
 */
class WrapRegEnable extends Module{
  val io = IO(new Bundle() {
    val in = Input(UInt(8.W))
    val enable = Input(Bool())
    val outWithInit = Output(UInt(8.W))
    val outNoInit = Output(UInt(8.W))
  })

  val r1 = RegEnable(io.in,255.U(8.W),io.enable)
  io.outWithInit := r1

  val r2 = RegEnable(io.in,io.enable)
  io.outNoInit := r2
}

class BasicTestRegEnable extends AnyFlatSpec with ChiselScalatestTester{
  behavior of "RegEnable"
  // test class body here
  it should "将检查RegEnable的输出" in {
    test(new WrapRegEnable){c=>
      (1 to 10) foreach(n=>{
        c.io.in.poke(n)
        println(s"$n:")
        if(n%2==0){print("enable is off. "); c.io.enable.poke(false)}
        else {print("enable is on. ");c.io.enable.poke(true)}
        c.clock.step()
        println("output with Init is "+c.io.outWithInit.peek().litValue)
        println("output with NoInit is "+c.io.outNoInit.peek().litValue)
      })

      println("After reset:")
      c.reset.poke(true.B)
      println("Enable is off.")
      c.io.enable.poke(false)
      c.clock.step()
      println("output with Init is " + c.io.outWithInit.peek().litValue)
      println("output with NoInit is " + c.io.outNoInit.peek().litValue)

      println("Enable is ON.")
      c.io.enable.poke(true)
      c.clock.step()
      println("output with Init is " + c.io.outWithInit.peek().litValue)
      println("output with NoInit is " + c.io.outNoInit.peek().litValue)

      println("Enable is ON.")
      c.io.enable.poke(true)
      c.clock.step()
      println("output with Init is " + c.io.outWithInit.peek().litValue)
      println("output with NoInit is " + c.io.outNoInit.peek().litValue)
    }
  }
}

/*
移位寄存器。它也有两种apply method
def apply[T <: Data](in: T, n: Int, en: Bool = true.B): T
def apply[T <: Data](in: T, n: Int, resetData: T, en: Bool): T
参数in，就是待移位的数据源，n是需要延迟的周期数（也可以理解为在数据的入口in，与出口之间有几个寄存器），en是移位的使能信号

有意思的是她的实现方式
ShiftRegister(in,3)
RegEnable(ShiftRegister(in,2),en)
RegEnable(RegEnable(ShiftRegister(in,1),en),en)
RegEnable(RegEnable(RegEnable(ShiftRegister(in,0),en),en),en)
RegEnable(RegEnable(RegEnable(in,en),en),en)
def apply[T <: Data](in: T, n: Int, en: Bool = true.B): T ={
  if(n != 0) {
    RegEnable(apply(in,n-1,en),en)
  } else {in}
}

关于时序应该这么理解，ShiftRegister以及RegEnable都是在时钟的上升沿同时当enable为高电平时，将数据源的信号推送到输出端
 */
class WrapShiftRegister extends Module{
  val io = IO(new Bundle() {
    val in = Input(UInt(8.W))
    val enable = Input(Bool())
    val out = Output(Vec(3,UInt(8.W)))
  })

  val shiftRegisterArray = (1 to 3) map(n=>ShiftRegister(io.in,n,0xff.U(8.W),io.enable))
  (1 to 3) foreach(n=> io.out(n-1):= shiftRegisterArray(n-1))
}

class BasicTestShiftRegister extends AnyFlatSpec with ChiselScalatestTester{
  behavior of "ShiftRegister"
  // test class body here
  it should "将检查ShiftRegister的输出(分别延迟 1 2 3 个时钟" in {
    test(new WrapShiftRegister) {c=>
      c.reset.poke(true.B)
      c.clock.step()
      c.reset.poke(false.B)
      c.io.enable.poke(false)
      c.clock.step()
      c.io.enable.poke(true)
      (1 to 10) foreach(n=>{
        //这里时钟的上升沿的同时输入端更新信号，使能信号一直为高电平，所以在时钟的上升沿到来的时候输出都会更新。相当于少了一个delay
        c.io.in.poke(n)
        c.clock.step()
        println(s"$n "+"output for shift register delayed by x1 clock:"+c.io.out(0).peek().litValue)
        println("output for shift register delayed by x2 clock:"+c.io.out(1).peek().litValue)
        println("output for shift register delayed by x3 clock:"+c.io.out(2).peek().litValue)
      })
    }
  }
}