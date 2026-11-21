// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
package pl.gov.obywatel.android.network.studiaonline

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudiaOnlineHostPolicyTest {
    @Test
    fun acceptsOnlyTheFirstPartyDomainAndItsSubdomains() {
        assertTrue(isAllowedStudiaOnlineHost("studia-online.pl"))
        assertTrue(isAllowedStudiaOnlineHost("www.studia-online.pl"))
        assertTrue(isAllowedStudiaOnlineHost("konto.studia-online.pl"))
    }

    @Test
    fun rejectsLookalikeAndUnrelatedHosts() {
        assertFalse(isAllowedStudiaOnlineHost("evilstudia-online.pl"))
        assertFalse(isAllowedStudiaOnlineHost("studia-online.pl.example.org"))
        assertFalse(isAllowedStudiaOnlineHost("example.org"))
        assertFalse(isAllowedStudiaOnlineHost(""))
    }
}
