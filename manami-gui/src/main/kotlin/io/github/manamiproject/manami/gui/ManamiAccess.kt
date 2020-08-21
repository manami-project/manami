package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.ManamiApp
import tornadofx.Controller

class ManamiAccess(val manami: ManamiApp = Manami()) : Controller(), ManamiApp by manami