(1 until 10).foreach(w => println(s"$w -> ${1.0 / w * ((1 << w) - 1) / (1 << w)}" ))