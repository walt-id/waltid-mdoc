package id.walt.test.integration

import io.kotest.core.spec.style.StringSpec


class ExcludedTest : StringSpec({

    "my excluded test" {
        throw RuntimeException("should be excluded")
    }

})

