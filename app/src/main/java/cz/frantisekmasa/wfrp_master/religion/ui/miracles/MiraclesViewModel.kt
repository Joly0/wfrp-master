package cz.frantisekmasa.wfrp_master.religion.ui.miracles

import cz.frantisekmasa.wfrp_master.common.core.domain.compendium.Compendium
import cz.frantisekmasa.wfrp_master.common.core.domain.identifiers.CharacterId
import cz.frantisekmasa.wfrp_master.common.core.viewModel.CharacterItemViewModel
import cz.frantisekmasa.wfrp_master.religion.domain.Miracle
import cz.frantisekmasa.wfrp_master.religion.domain.MiracleRepository
import cz.frantisekmasa.wfrp_master.compendium.domain.Miracle as CompendiumMiracle

internal class MiraclesViewModel(
    characterId: CharacterId,
    repository: MiracleRepository,
    compendium: Compendium<CompendiumMiracle>,
) : CharacterItemViewModel<Miracle, CompendiumMiracle>(characterId, repository, compendium)
