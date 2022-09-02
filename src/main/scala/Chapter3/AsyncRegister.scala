package Chapter3
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util.{RegEnable, ShiftRegister}
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/*
异步寄存器。
也可以用withClock,withReset来单独控制时钟和reset。
 */
class AsyncRegister extends Module {
  val io = IO(new Bundle() {
    val asyncClk = Input(UInt(1.W))
    val asyncRst = Input(UInt(1.W))
    val out = Output(UInt(8.W))
  })

  val asyncRegInit = withClockAndReset(io.asyncClk.asBool.asClock,io.asyncRst.asBool.asAsyncReset)(RegInit(255.U(8.W)))
  asyncRegInit := asyncRegInit + 1.U(8.W)
  io.out := asyncRegInit
}

class BasicTestAsyncRegister extends AnyFlatSpec with ChiselScalatestTester{
  behavior of "AsyncRegister"
  // test class body here
  it should "异步寄存器，它的时钟由asyncClk单独控制，重启信号来自asyncRst" in{
    test(new AsyncRegister){c=>
      c.io.asyncRst.poke(0)
      (1 to 100) foreach(n=>{
        c.io.asyncClk.poke(scala.util.Random.nextInt(2))
        println(c.io.out.peek().litValue)
        c.clock.step()
      })
    }
  }
}

object GenerateVCDForAsyncRegister extends App{
//  (new ChiselStage).execute(Array("--generate-vcd-output","on","--backend-name","verilator"),Seq(ChiselGeneratorAnnotation(()=>{
//    val c = new AsyncRegister
//    c.io.asyncRst.poke(0)
//    (1 to 100) foreach (n => {
//      c.io.asyncClk.poke(scala.util.Random.nextInt(2))
//      println(c.io.out.peek().litValue)
//      c.clock.step()
//    })
//    c
//  })))
}
