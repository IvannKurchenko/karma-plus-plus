package com.plus.plus.karma

import cats.effect._

/*
object ExampleSix extends IOApp {
  override def run: IO[Unit] =
    for {
      latch <- Latch(10)
      _ <- (1 to 10).toList.traverse { idx =>
        (IO.println(s"$idx counting down") *> latch.release).start
      }
      _ <- latch.await
      _ <- IO.println("Got past the latch")
    } yield ()
}
*/
