# JACAMOb

Then, we instantiate the MAOPb in an extended version of JaCaMo, adding a new component to the framework core, the ageNt Embodied Cognition development Kit (NECK). 

This component accepts physical body descriptions and loads the agents’ bodies from the project specification. 
An overview of the JACAMOb , is shown in Figure below. The body description receives the name of the embodied agent and a set of apparatus that constitute the agent’s body, with their respective implementation. NECK continuously collects data from the elements that compose each apparatus and converts them into percepts automatically injected into the agent’s belief base at each reasoning cycle, thereby implementing a passive perception approach.

<img width="1517" height="385" alt="image" src="https://github.com/user-attachments/assets/a03f480d-a211-4b4d-aad9-34dcacaae909" />


Additionally, we adjusted the grammar of the JCM project by adding body and apparatus definitions. In Code below, an example of a JCM +b project specification. Now, every agent can have a body, and during the project loading, the interpreter checks
whether a body is specified for that agent (lines 5-7). If so, the body with all assembled apparatus is bound to the agent. Therefore, perceptions from the body are transmitted directly to the agent’s mind, updating the BB at each reasoning cycle. 

<img width="1079" height="448" alt="image" src="https://github.com/user-attachments/assets/c9d1bf57-26a6-4281-a078-9a9b932dfb65" />

## COPYRIGHT
<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />JACAMOb is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>. The licensor cannot revoke these freedoms as long as you follow the license terms:

* __Attribution__ — You must give __appropriate credit__ like below:

Nilson Lazarin, Carlos Pantoja, and Jose Viterbo. 2026. My Body, My Perceptions: A Shift from Computationalism to Embodied Cognition in BDI-agent-based Embedded Systems. In Proc. of the 25th International Conference on Autonomous Agents and Multiagent Systems (AAMAS 2026), Paphos, Cyprus, May 25 – 29, 2026, IFAAMAS, 10 pages. DOI: [10.65109/QIVX3835](https://doi.org/10.65109/QIVX3835)
