package mj.carthy.easyutils

import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.AbstractProjectConfig

internal open class BaseMainTest: AbstractProjectConfig() {

  @ExperimentalKotest
  override val concurrentTests: Int = 100
  override val parallelism: Int = 1
}